/**
 * Mirai Tablegames Plugin
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

public class GraveKeeperBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3372771051275452272L;
	String checked = null;
	boolean exposed = false;

	public GraveKeeperBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPublic(String msg) {
		if ((checked != null || exposed) && msg.contains(GenericBot.talkKey)) {
			game.getScheduler().execute(() -> {
				if (exposed == false) {
					sendBotMessage("守墓人牌");
					exposed = true;
				}
				if (checked != null) {
					sendBotMessage(checked);
				} else {
					sendBotMessage("昨天没有驱逐人。");
					super.sendAtAsBot(" 过");
				}
			});
		}
		super.onPublic(msg);
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("前一天没有驱逐人")) {
			checked = null;
			return;
		} else if (msg.contains("上一个驱逐的是")) {
			checked = msg;
			return;
		}
		super.onPrivate(msg);
	}

}
