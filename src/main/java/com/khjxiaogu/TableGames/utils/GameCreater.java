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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.utils;

import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
@FunctionalInterface
public interface GameCreater<T extends Game> {
	public T createGame(AbstractRoom group, int count);

	public default T createGame(AbstractRoom gp, String... args) {
		return createGame(gp,Integer.parseInt(args[0]));
	};

	public default T createGame(AbstractRoom gp,int cplayer,Map<String,String> args) {
		return createGame(gp,cplayer);
	};
}
