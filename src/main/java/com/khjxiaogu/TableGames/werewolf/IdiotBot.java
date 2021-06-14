package com.khjxiaogu.TableGames.werewolf;

public class IdiotBot extends GenericBot {

	public IdiotBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPublic(String msg) {

		if (msg.contains(GenericBot.talkKey)) {
			sendBotMessage("白神");
			super.sendAtAsBot(" 过");
		} else {
			super.onPublic(msg);
		}
	}
}
