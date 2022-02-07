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
package com.khjxiaogu.TableGames.game.werewolf;

import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.GameCreater;

public class StandardWerewolfCreater implements GameCreater<WerewolfGame> {
	static Map<String, String> dconfig = new HashMap<>();
	static {
		dconfig.put("屠城", "false");
		dconfig.put("板", "标准");

		dconfig.put("空刀", "true");
	}

	@Override
	public WerewolfGame createGame(AbstractRoom group, int count) {
		return new WerewolfGame(group, count, dconfig);
	}

	@Override
	public WerewolfGame createGame(AbstractRoom gp, String... args) {
		return new WerewolfGame(gp, 12, dconfig);
	}

	@Override
	public WerewolfGame createGame(AbstractRoom gp, int cplayer, Map<String, String> args) {
		args.putAll(dconfig);
		return new WerewolfGame(gp, cplayer, args);
	}

}
