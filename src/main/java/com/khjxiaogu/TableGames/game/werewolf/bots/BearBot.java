package com.khjxiaogu.TableGames.game.werewolf.bots;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class BearBot extends GenericBot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 528608808684822537L;

	public BearBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPublic(String msg) {
		if (msg.contains(GenericBot.talkKey)) {
			sendBotMessage("熊牌。");
			super.sendAtAsBot(" 过");
		}
	}
}
