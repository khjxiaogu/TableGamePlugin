package com.khjxiaogu.TableGames.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractRoom;

import net.mamoe.mirai.contact.Group;

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

	public static <T extends Game> T createGame(Class<T> class1, AbstractRoom group, int count) {
		Game g = GameUtils.getGames().get(group);
		synchronized (GameUtils.getGames()) {
			if (g != null && g.isAlive()) {
				g.forceStop();
			}
			T ng = null;
			try {
				ng = class1.getConstructor(Group.class, int.class).newInstance(group, count);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			GameUtils.getGames().put(group, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(Class<T> gameClass, AbstractRoom gp, String... args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
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
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}

	public static <T extends Game> T createGame(Class<T> gameClass, AbstractRoom gp,int cplayer,Map<String,String> args) {
		Game g = GameUtils.getGames().get(gp);
		synchronized (GameUtils.getGames()) {
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
			GameUtils.getGames().put(gp, ng);
			return ng;
		}
	}

}
