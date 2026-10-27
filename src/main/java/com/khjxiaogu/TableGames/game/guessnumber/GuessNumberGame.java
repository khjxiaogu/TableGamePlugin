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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * 游戏开始后群里所有人共用一个答案，直接发送数字即可参与，
 * 谁先猜中谁就获胜并且游戏立刻结束；一段时间内无人猜中则公布答案并结束。
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
	/** 无人猜中的等待时间，超时即结束游戏并公布答案 */
	public static final long IDLE_TIMEOUT = 120000L;
	/** 输家支付给赢家的积分，没有积分则不用支付 */
	static final double LOSS = 0.1;

	/** 用 UserIdentifier 作 key，避免不同事件里的用户包装对象不一致 */
	Map<UserIdentifier, GuessNumberPlayer> user = new ConcurrentHashMap<>();
	int cplayer;
	boolean started;
	volatile boolean isAlive;
	/** 是否被强制停止，强制停止后不再结算 */
	volatile boolean stopped;
	/** 是否已经有人猜中 */
	volatile boolean finished;
	/** 最后一次有效猜测的时间，超过该时间游戏结束 */
	volatile long lastActive;
	transient Thread gameThread;

	/** 本局答案 */
	int target;
	/** 全员共用的可行区间 */
	int low = MIN;
	int high = MAX;
	/** 本局所有玩家猜过的数字，不允许重复猜 */
	Set<Integer> guessed = Collections.newSetFromMap(new ConcurrentHashMap<>());
	/** 赢家 */
	UserIdentifier winner;

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
			target = randomTarget();
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
		if (!started || stopped || finished || mem == null)
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
				+ "所有玩家共用一个答案，谁先猜中谁就获胜，游戏立刻结束！猜错会告诉您偏大还是偏小。\n"
				+ "获胜者会从其他每位参与者那里赢得 " + LOSS + " 积分。\n"
				+ IDLE_TIMEOUT / 60000 + "分钟内没有人猜中，游戏自动结束并公布答案。");
		this.getGroup().registerRoomListener(this, (u, msg, type) -> {
			// 普通聊天是 PUBLIC，@机器人 或者 ##指令 是 AT
			if (type == MsgType.PUBLIC || type == MsgType.AT)
				handleGuess(u, msg.getText());
		});
		lastActive = TimeUtil.getTime() + IDLE_TIMEOUT;
		while (!stopped && !finished) {
			try {
				Thread.sleep(1000);
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
		GuessNumberPlayer gp = user.computeIfAbsent(u.getId(), id -> new GuessNumberPlayer(u));
		gp.setMember(u);
		return gp;
	}

	private static int randomTarget() {
		return ThreadLocalRandom.current().nextInt(MIN, MAX + 1);
	}

	public synchronized void checkGuess(GuessNumberPlayer gp, int guess) {
		if (!isAlive || finished)
			return;
		if (guess < MIN || guess > MAX) {
			gp.sendPublic("只能猜 " + MIN + "~" + MAX + " 之间的整数噢！");
			return;
		}
		if (!guessed.add(guess)) {
			gp.sendPublic("数字 " + guess + " 已经猜过了！答案在 " + low + "~" + high + " 之间。");
			return;
		}
		gp.guessCount++;
		lastActive = TimeUtil.getTime() + IDLE_TIMEOUT;
		if (guess == target) {
			// 猜中即获胜，游戏立刻结束
			gp.pt++;
			winner = gp.getId();
			finished = true;
			sendPublicMessage("恭喜 " + gp.getMemberString() + " 猜中了！答案就是 " + target + "！");
			Thread thread = gameThread;
			if (thread != null)
				thread.interrupt();
			return;
		}
		if (guess > target)
			high = Math.min(high, guess - 1);
		else
			low = Math.max(low, guess + 1);
		gp.sendPublic((guess > target ? "大了！" : "小了！") + "答案在 " + low + "~" + high + " 之间。");
	}

	private void finish() {
		sendPublicMessage("游戏结束！");
		if (user.isEmpty()) {
			sendPublicMessage("本局没有玩家参与，答案是 " + target + "，欢迎下次再来！");
			return;
		}
		List<GuessNumberPlayer> ranked = new ArrayList<>(user.values());
		ranked.sort((a, b) -> {
			int cmp = Integer.compare(b.pt, a.pt);
			if (cmp != 0)
				return cmp;
			cmp = Integer.compare(b.guessCount, a.guessCount);
			if (cmp != 0)
				return cmp;
			return a.getId().serialize().compareTo(b.getId().serialize());
		});
		GuessNumberPlayer wp = winner == null ? null : user.get(winner);
		if (wp != null) {
			settleCredit(wp);
			sendPublicMessage("本局获胜者是 " + wp.getMemberString() + "，一共猜了 " + wp.guessCount + " 次！");
		} else {
			sendPublicMessage("本局没有人猜中，答案是 " + target + "。");
		}
		StringBuilder sb = new StringBuilder("游戏结果：");
		int rank = 1;
		for (GuessNumberPlayer gp : ranked)
			sb.append("\n").append(rank++).append(". ").append(gp.getMemberString()).append(" 猜中").append(gp.pt)
					.append("次 猜测").append(gp.guessCount).append("次 ")
					.append(PlayerCredit.normalizedb(gp.ptchange)).append("分");
		sendPublicMessage(sb.toString());
	}

	/**
	 * 赢家从其他每位参与者处赢得 LOSS 积分，参与者没有积分则不扣。
	 */
	private void settleCredit(GuessNumberPlayer winner) {
		if (GlobalMain.credit == null)
			return;
		double gain = 0;
		for (GuessNumberPlayer gp : user.values()) {
			if (gp == winner)
				continue;
			PlayerCredit pc = GlobalMain.credit.get(gp.getId());
			double pay = Math.min(LOSS, pc.getPT());
			if (pay > 0 && pc.withdrawPT(PlayerCredit.normalizedb(pay)) >= 0) {
				gain += pay;
				gp.ptchange = -pay;
			}
		}
		gain = PlayerCredit.normalizedb(gain);
		if (gain > 0) {
			GlobalMain.credit.get(winner.getId()).givePT(gain);
			winner.ptchange = gain;
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
