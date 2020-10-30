package com.khjxiaogu.TableGames.data;

public interface GenericPlayerData<T extends GenericPlayerData<T>>{
	public void plus(T another);
	@SuppressWarnings("unchecked")
	public default void plusa(GenericPlayerData<?> another) {
		if(another!=null) {
			plus((T) another);
		}
	}
}
