package com.khjxiaogu.TableGames.spwarframe;

public interface Platform {
	public void waitTime(long time);
	public void skipWait();
	public void sendAll(String s);
	public void sendAllLong(String s);
}
