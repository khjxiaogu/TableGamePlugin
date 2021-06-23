package com.khjxiaogu.TableGames.data;

public interface GenericPlayerData<T extends GenericPlayerData<T>>{
	void plus(T another);
	@SuppressWarnings("unchecked")
	default void plusa(GenericPlayerData<?> another) {
		if(another!=null) {
			plus((T) another);
		}
	}
}
