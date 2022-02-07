/**
 * Mirai Song Plugin
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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
