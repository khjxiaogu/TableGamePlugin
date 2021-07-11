package com.khjxiaogu.TableGames.utils;

import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;

public interface GameCreater<T extends Game> {
	public T createGame(AbstractRoom group, int count);

	public T createGame(AbstractRoom gp, String... args);

	public T createGame(AbstractRoom gp,int cplayer,Map<String,String> args);
}
