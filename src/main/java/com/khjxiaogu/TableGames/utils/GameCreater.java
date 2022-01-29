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
