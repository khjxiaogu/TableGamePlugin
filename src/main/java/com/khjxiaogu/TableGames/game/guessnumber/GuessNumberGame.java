/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.guessnumber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import com.khjxiaogu.TableGames.data.application.PlayerCredit;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.PreserveLess;
import com.khjxiaogu.TableGames.utils.TimeUtil;
import com.khjxiaogu.TableGames.utils.Utils;

/**
 * 无需预约的0~999猜数字游戏。
 * <p>
 * 游戏开始后群里所有人都可以直接发送数字参与，每人有独立的答案，
 * 猜中即得分并换上新答案，一段时间内无人猜中则游戏结束，猜中次数最多的人获胜。
 */
public class GuessNumberGame extends Game implements PreserveLess {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3395030973861155140L;
	/** 可猜的最小数字 */
	public static final int MIN = 0;
	/** 可猜的最大数字 */
	public static final int MAX = 999;
	/** 无人猜中的等待时间，超时即结束游戏 */
	public static final long IDLE_TIMEOUT = 120000L;
	/** 非法猜测的小额惩罚，与成语接龙保持一致 */
	static final double PENALTY = 0.01;

	/** 用 UserIdentifier 作 key，避免不同事件里的用户包装对象不一致 */
	Map<UserIdentifier, GuessNumberPlayer> user = new ConcurrentHashMap<>();
	int cplayer;
	boolean started;
	volatile boolean isAlive;
	/** 是否被强制停止，强制停止后不再结算 */
	volatile boolean stopped;
	/** 最后一次有效猜测的时间，超过该时间游戏结束 */
	volatile long lastActive;
	transient Thread gameThread;

	public GuessNumberGame(AbstractRoom group, int cplayer) {
		super(group, cplayer, 4);
		this.cplayer = cplayer;
	}

	@Override
	public void forceStart() {
		synchronized (this) {
			if (started)
				return;
			started = true;
		}
		gameThread = new Thread(this::gameMain, "GuessNumber-" + getGroup().getId().serialize());
		gameThread.start();
	}

	@Override
	public void startEmpty() {
		forceStart();
	}

	@Override
	public String getName() {
		return "猜数字";
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public boolean onReAttach(UserIdentifier id) {
		return false;
	}

	@Override
	public boolean addMember(AbstractUser mem) {
		if (!started || mem == null)
			return false;
		join(mem);
		mem.sendPublic("您已经加入猜数字游戏！发送 " + MIN + "~" + MAX + " 之间的数字来猜吧！");
		return true;
	}

	public void gameMain() {
		// 可能在线程真正运行之前就被停止了
		if (stopped)
			return;
		isAlive = true;
		sendPublicMessage("猜数字游戏开始！\n"
				+ "本游戏无需预约，直接在群里发送 " + MIN + "~" + MAX + " 之间的数字就可以参与，也可以发送“猜 500”这样的格式。\n"
				+ "每位玩家都有自己独立的答案，猜中就会换上新答案并且得1分。\n"
				+ "同一轮里猜过的数字不能重复猜，猜错会告诉您偏大还是偏小。\n"
				+ IDLE_TIMEOUT / 60000 + "分钟内没有人猜数字，游戏结束，猜中次数最多的玩家获胜！");
		this.getGroup().registerRoomListener(this, (u, msg, type) -> {
			// 普通聊天是 PUBLIC，@机器人 或者 ##指令 是 AT
			if (type == MsgType.PUBLIC || type == MsgType.AT)
				handleGuess(u, msg.getText());
		});
		lastActive = TimeUtil.getTime() + IDLE_TIMEOUT;
		while (!stopped) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				break;
			}
			if (TimeUtil.getTime() >= lastActive)
				break;
		}
		isAlive = false;
		this.getGroup().releaseRoomListener(this);
		if (stopped)
			return;
		finish();
	}

	/**
	 * 解析群消息，只有确实是猜数字的消息才会被处理。
	 */
	private void handleGuess(AbstractUser u, String text) {
		if (!isAlive || u == null || text == null)
			return;
		String body = text.trim();
		boolean attempt = false;
		if (body.startsWith("我猜")) {
			body = Utils.removeLeadings("我猜", body);
			attempt = true;
		} else if (body.startsWith("猜")) {
			body = Utils.removeLeadings("猜", body);
			attempt = true;
		}
		if (!body.matches("[0-9]{1,3}")) {
			if (attempt)
				u.sendPublic("请发送 " + MIN + "~" + MAX + " 之间的整数来猜数字噢！");
			return;
		}
		checkGuess(join(u), Integer.parseInt(body));
	}

	private GuessNumberPlayer join(AbstractUser u) {
		GuessNumberPlayer gp = user.computeIfAbsent(u.getId(), id -> new GuessNumberPlayer(u, randomTarget(-1)));
		gp.setMember(u);
		return gp;
	}

	private static int randomTarget(int previous) {
		int target = ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
		if (target == previous)
			target = target == MAX ? MIN : target + 1;
		return target;
	}

	public synchronized void checkGuess(GuessNumberPlayer gp, int guess) {
		if (!isAlive)
			return;
		if (guess < MIN || guess > MAX) {
			gp.sendPublic("只能猜 " + MIN + "~" + MAX + " 之间的整数噢！");
			return;
		}
		if (!gp.guessed.add(guess)) {
			gp.sendPublic("数字 " + guess + " 您本轮已经猜过了！答案在 " + gp.low + "~" + gp.high + " 之间。");
			return;
		}
		lastActive = TimeUtil.getTime() + IDLE_TIMEOUT;
		if (guess == gp.target) {
			gp.pt+=0.2;
			gp.sendPublic("猜中了！答案就是 " + guess + "！您已经猜中 " + gp.pt + " 次。\n新一轮开始，请继续猜 "
					+ MIN + "~" + MAX + " 的数字！");
			gp.newRound(randomTarget(guess));
		} else if (guess > gp.target) {
			gp.high = Math.min(gp.high, guess - 1);
			gp.sendPublic("大了！答案在 " + gp.low + "~" + gp.high + " 之间。");
		} else {
			gp.low = Math.max(gp.low, guess + 1);
			gp.sendPublic("小了！答案在 " + gp.low + "~" + gp.high + " 之间。");
		}
	}

	private void finish() {
		sendPublicMessage("游戏结束！");
		if (user.isEmpty()) {
			sendPublicMessage("本局没有玩家参与，欢迎下次再来！");
			return;
		}
		List<GuessNumberPlayer> ranked = new ArrayList<>(user.values());
		ranked.sort((a, b) -> {
			int cmp = Integer.compare(b.pt, a.pt);
			return cmp != 0 ? cmp : a.getId().serialize().compareTo(b.getId().serialize());
		});
		int total = 0;
		for (GuessNumberPlayer gp : ranked)
			total += gp.pt;
		cplayer = ranked.size();
		distributeCredit(ranked, total);
		StringBuilder sb = new StringBuilder("游戏结果：");
		GuessNumberPlayer best = ranked.get(0);
		if (best.pt > 0) {
			StringBuilder names = new StringBuilder();
			for (GuessNumberPlayer gp : ranked) {
				if (gp.pt != best.pt)
					break;
				if (names.length() > 0)
					names.append("、");
				names.append(gp.getMemberString());
			}
			sb.append("\n本局猜中次数最多的是 ").append(names).append("，共猜中 ").append(best.pt).append(" 次！");
		}
		int rank = 1;
		for (GuessNumberPlayer gp : ranked)
			sb.append("\n").append(rank++).append(". ").append(gp.getMemberString()).append(" 猜中").append(gp.pt)
					.append("次 ").append(PlayerCredit.normalizedb(gp.ptchange)).append("分");
		sendPublicMessage(sb.toString());
	}

	/**
	 * 按猜中次数的均值把积分从低于均值的人转移给高于均值的人，与成语接龙的结算方式一致。
	 */
	private void distributeCredit(List<GuessNumberPlayer> players, int total) {
		int count = players.size();
		// 参与度不够时不做积分结算
		if (GlobalMain.credit == null || count == 0 || total < 4 * count)
			return;
		double avg = (double) total / count;
		double maxpt = 0;
		double minpt = 0;
		List<GuessNumberPlayer> max = new ArrayList<>();
		List<GuessNumberPlayer> min = new ArrayList<>();
		for (GuessNumberPlayer gp : players) {
			if (gp.pt < avg) {
				minpt += avg - gp.pt;
				min.add(gp);
			} else {
				maxpt += gp.pt - avg;
				max.add(gp);
			}
		}
		double pt = 0.5 + (double) total / count * 0.01;
		double tpm = 0.25 * count + (maxpt + minpt) / count * 0.1;
		for (GuessNumberPlayer gp : min) {
			PlayerCredit pc = GlobalMain.credit.get(gp.getId());
			double ppm;
			if (minpt > 0)
				ppm = tpm * ((avg - gp.pt) / minpt);
			else
				ppm = tpm / min.size();
			if (pc.getPT() >= ppm) {
				if (pc.withdrawPT(PlayerCredit.normalizedb(ppm)) >= 0)
					pt += ppm;
			} else if (pc.getPT() > 0) {
				ppm = pc.getPT();
				pc.withdrawPT(ppm);
				pt += ppm;
			} else {
				ppm = 0;
			}
			gp.ptchange = -ppm;
		}
		for (GuessNumberPlayer gp : max) {
			double ppm;
			if (maxpt > 0)
				ppm = pt * ((gp.pt - avg) / maxpt);
			else
				ppm = pt / max.size();
			GlobalMain.credit.get(gp.getId()).givePT(PlayerCredit.normalizedb(ppm));
			gp.ptchange = ppm;
		}
	}

	@Override
	public void forceStop() {
		stopped = true;
		isAlive = false;
		Thread thread = gameThread;
		if (thread != null)
			thread.interrupt();
		this.getGroup().releaseRoomListener(this);
		super.forceStop();
	}
}
