package com.khjxiaogu.TableGames.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.Game;

import net.mamoe.mirai.contact.Group;

public class GameUtils {

	static Map<Group, Game> gs = new ConcurrentHashMap<>();
	public static Set<Long> ingame = Collections.newSetFromMap(new ConcurrentHashMap<>());

	public GameUtils() {
	}

	public static Map<Group, Game> getGames() {
		return gs;
	}

	public static boolean tryAddMember(Long id) {
		return ingame.add(id);
		//return true;
	}

	public static boolean hasMember(Long id) {
		return ingame.contains(id);
		//return false;
	}

	public static void RemoveMember(Long id) {
		ingame.remove(id);
	}

	public static boolean hasActiveGame(Group gp) {
		Game g = getGames().get(gp);
		if (g != null && g.isAlive())
			return true;
		return false;
	}

	public static <T extends Game> T createGame(Class<T> gameClass, Group gp, int count) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class, int.class).newInstance(gp, count);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(Class<T> gameClass, Group gp, String... args) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class, String[].class).newInstance(gp, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(Class<T> gameClass, Group gp,int cplayer,Map<String,String> args) {
		Game g = getGames().get(gp);
		synchronized (getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = gameClass.getConstructor(Group.class,int.class,Map.class).newInstance(gp,cplayer, args);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			getGames().put(gp, ng);
			return ng;
		}
	}

}
