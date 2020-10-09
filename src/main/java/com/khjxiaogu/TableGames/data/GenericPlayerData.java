package com.khjxiaogu.TableGames.data;

public interface GenericPlayerData<T extends GenericPlayerData<T>>{
	public void plus(T another);
	public default void plusa(GenericPlayerData<?> another) {
		if(another instanceof GenericPlayerData<?>) {
			plus((T) another);
		}
	}
}
