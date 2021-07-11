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
