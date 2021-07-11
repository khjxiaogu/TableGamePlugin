package com.khjxiaogu.TableGames.game.werewolf.bots;

import com.khjxiaogu.TableGames.game.werewolf.Elder;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class ElderBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3837205811213497566L;
	boolean exposed = false;
	boolean lostlife = false;

	public ElderBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPublic(String msg) {
		if (msg.contains(GenericBot.talkKey)) {
			if (game.getAliveCount() <= 6) {
				exposed = true;
			}
			if (shouldTellElder()) {
				sendBotMessage("长老牌，昨晚没死人，大概是挡刀了。");
				exposed = true;
			} else if (exposed) {
				sendBotMessage("长老牌");
			}
			if (exposed) {
				super.sendAtAsBot(" 过");
				return;
			}

		} else {
			super.onPublic(msg);
		}
	}

	public boolean shouldTellElder() {
		Elder me = (Elder) super.getPlayer().getRoleObject();
		boolean res = false;
		if (me.lifeUsed) {
			if (!lostlife && game.getLastDeathCount() == 0) {
				res = true;
			}
			lostlife = true;
		}
		return res;
	}
}
