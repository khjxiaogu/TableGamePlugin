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
package com.khjxiaogu.TableGames.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;

public class DefaultGameCreater<T extends Game> implements GameCreater<T> {
	Class<T> gameClass;
	public DefaultGameCreater(Class<T> gameClass) {
		this.gameClass = gameClass;
	}

	public T createGame(AbstractRoom group, int count) {
		Game g = GameUtils.getGames().get(group);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(AbstractRoom.class, int.class).newInstance(group, count);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GameUtils.getGames().put(group, ng);
			return ng;
		}
	}

	public T createGame(AbstractRoom gp, String... args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(AbstractRoom.class, String[].class).newInstance(gp, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}

	public T createGame(AbstractRoom gp,int cplayer,Map<String,String> args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(AbstractRoom.class,int.class,Map.class).newInstance(gp,cplayer, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}
}
