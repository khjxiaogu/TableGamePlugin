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
