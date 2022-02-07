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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
public class GameUtils {

	static Map<AbstractRoom, Game> gs = new ConcurrentHashMap<>();
	public static Set<UserIdentifier> ingame = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public GameUtils() {
	}

	public static Map<AbstractRoom, Game> getGames() {
		return GameUtils.gs;
	}

	public static boolean tryAddMember(UserIdentifier id) {
		return GameUtils.ingame.add(id);
		//return true;
	}

	public static boolean hasMember(UserIdentifier id) {
		return GameUtils.ingame.contains(id);
		//return false;
	}

	public static void RemoveMember(UserIdentifier id) {
		GameUtils.ingame.remove(id);
	}

	public static boolean hasActiveGame(AbstractRoom gp) {
		Game g = GameUtils.getGames().get(gp);
		if (g != null && g.isAlive())
			return true;
		return false;
	}

	public static <T extends Game> T createGame(GameCreater<T> gc, AbstractRoom group, int count) {
		Game g = GameUtils.getGames().get(group);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = gc.createGame(group, count);
			GameUtils.getGames().put(group, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(GameCreater<T> gc, AbstractRoom gp, String... args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng =gc.createGame(gp, args);
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(GameCreater<T> gc, AbstractRoom gp,int cplayer,Map<String,String> args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = gc.createGame(gp, cplayer, args);
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}

}
