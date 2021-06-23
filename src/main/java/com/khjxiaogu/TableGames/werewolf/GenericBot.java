package com.khjxiaogu.TableGames.werewolf;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.khjxiaogu.TableGames.platform.mirai.MiraiBotPlayer;

public class GenericBot extends MiraiBotPlayer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6801990493391227114L;
	WerewolfGame game;
	static Random rnx = new SecureRandom();
	public static final String talkKey = "五分钟时间进行陈述";
	public static final String deadKey = "说出你的遗言";
	Random rnd = new Random(GenericBot.rnx.nextLong());

	@Override
	public void onPublic(String msg) {
		super.onPublic(msg);
		if (msg.contains(GenericBot.talkKey)) {
			sendAtAsBot("好人牌，过。");
		} else if (msg.contains("说出你的遗言")) {
			if (((Villager) super.roleObject).getFraction() != Fraction.Wolf) {
				sendBotMessage("我是" + ((Villager) super.roleObject).getRole());
			} else {
				sendBotMessage("我是平民");
			}
			sendAtAsBot(" 过");
		}
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.startsWith("请私聊投票")) {
			List<Villager> canvote = game.canVote;
			if (canvote == null) {
				canvote = new ArrayList<>(game.playerlist);
			} else {
				canvote = new ArrayList<>(canvote);
			}
			canvote.removeIf(v -> v.isDead);
			canvote.removeIf(v -> v == roleObject);

			boolean isWolf = rnd.nextInt(3)<3;
			if (isWolf) {
				canvote.removeIf(v -> !v.getFraction().equals(Fraction.Wolf));
				if (canvote.size() == 0) {
					this.sendAsBot("弃权");
				}
				this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
			} else {
				canvote.removeIf(v -> v.getFraction().equals(Fraction.Wolf));
				if (canvote.size() == 0) {
					this.sendAsBot("弃权");
					return;
				}
				this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
			}
		}
		super.onPrivate(msg);
	}

	public GenericBot(int botId, WerewolfGame gam) {
		super(botId, gam.getGroup(), gam);
		game = gam;
	}

	@Override
	public void sendAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			super.sendAsBot(msg);
		}, 5000);
	}

	@Override
	public void sendAtAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			super.sendAtAsBot("@" +game.getGroup().getHostNameCard() + msg);
		}, 6000);
	}

	@Override
	public void sendBotMessage(String msg) {
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.sendBotMessage(msg);

	}

}
