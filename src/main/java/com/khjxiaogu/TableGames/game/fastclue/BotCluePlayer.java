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
package com.khjxiaogu.TableGames.game.fastclue;

import java.util.Random;

import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.BotUser;


public class BotCluePlayer extends BotUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = -748442401094635373L;
	FastClueGame game;
	static Random trnd=new Random();
	Random botrandom=new Random(BotCluePlayer.trnd.nextLong());
	public BotCluePlayer(AbstractBotUser player,FastClueGame in) {
		super(player);
		game=in;
	}

	@Override
	public void onPrivate(String msg) {
		//game.getGroup().getBot().getLogger().info(msg);
		if(msg.startsWith("你可以")) {
			int rgp=botrandom.nextInt(game.players.size());
			int rrl=botrandom.nextInt(game.rooms.size());
			int rwp=botrandom.nextInt(game.weapons.size());
			game.getScheduler().execute(()->super.getPlayer().sendAsBot("假设 "+rgp+""+rrl+""+rwp));
		}else
			if(msg.startsWith("格式")) {
				game.getScheduler().execute(()->super.getPlayer().sendAsBot("放弃"));
			}
	}

}
