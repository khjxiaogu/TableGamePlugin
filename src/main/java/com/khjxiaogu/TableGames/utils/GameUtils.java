package com.khjxiaogu.TableGames.utils;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
public class GameUtils {

	static Map<AbstractRoom, Game> gs = new ConcurrentHashMap<>();
	public static Set<Long> ingame = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public GameUtils() {
	}

	public static Map<AbstractRoom, Game> getGames() {
		return GameUtils.gs;
	}

	public static boolean tryAddMember(Long id) {
		return GameUtils.ingame.add(id);
		//return true;
	}

	public static boolean hasMember(Long id) {
		return GameUtils.ingame.contains(id);
		//return false;
	}

	public static void RemoveMember(Long id) {
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
