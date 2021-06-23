package com.khjxiaogu.TableGames.werewolf;

import java.security.SecureRandom;
import java.util.Random;

import com.khjxiaogu.TableGames.platform.mirai.MiraiBotPlayer;

public class DeadBot extends MiraiBotPlayer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5613603875937581111L;
	WerewolfGame game;
	static Random rnx = new SecureRandom();
	public static final String talkKey = "五分钟时间进行陈述";
	public static final String deadKey = "说出你的遗言";
	Random rnd = new Random(DeadBot.rnx.nextLong());

	@Override
	public void onPublic(String msg) {
	}

	@Override
	public void onPrivate(String msg) {
	}

	public DeadBot(int botId, WerewolfGame gam) {
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
			super.sendAtAsBot("@" + game.getGroup().getHostNameCard() + msg);
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
