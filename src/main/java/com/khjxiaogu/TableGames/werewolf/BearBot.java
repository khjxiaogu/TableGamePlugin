package com.khjxiaogu.TableGames.werewolf;

public class BearBot extends GenericBot {

	public BearBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPublic(String msg) {
		if (msg.contains(GenericBot.talkKey)) {
			sendBotMessage("熊牌。");
			super.sendAtAsBot(" 过");
		}
	}
}
