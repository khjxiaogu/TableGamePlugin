package com.khjxiaogu.TableGames.game.werewolf.bots;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class IdiotBot extends GenericBot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8568641646529938940L;

	public IdiotBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

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
