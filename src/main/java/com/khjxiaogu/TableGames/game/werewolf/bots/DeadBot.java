package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.security.SecureRandom;
import java.util.Random;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.BotUser;

public class DeadBot extends BotUser {
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

	public DeadBot(AbstractBotUser p, WerewolfGame gam) {
		super(p);
		game = gam;
	}

	public void sendAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			getPlayer().sendAsBot(msg);
		}, 5000);
	}

	public void sendAtAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			getPlayer().sendAtAsBot("@" + game.getGroup().getHostNameCard() + msg);
		}, 6000);
	}

	public void sendBotMessage(String msg) {
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getPlayer().sendBotMessage(msg);

	}

}
