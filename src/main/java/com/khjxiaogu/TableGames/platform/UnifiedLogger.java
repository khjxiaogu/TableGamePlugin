package com.khjxiaogu.TableGames.platform;

public interface UnifiedLogger {
	void debug(String msg);
	void info(String msg);
	void warning(String msg);
	void error(String msg);
	void severe(String msg);
	void error(Exception e);
}
