package com.khjxiaogu.TableGames.utils;

import com.khjxiaogu.TableGames.platform.UnifiedLogger;

public class SimpleGameLogger implements UnifiedLogger {

	SimpleLogger logger;
	public SimpleGameLogger(String name) {
		logger=new SimpleLogger(name);
	}
	@Override
	public void debug(String msg) {
		logger.config(msg);
	}
	@Override
	public void info(String msg) {
		logger.info(msg);
	}
	@Override
	public void warning(String msg) {
		logger.warning(msg);
	}
	@Override
	public void error(String msg) {
		logger.error(msg);
	}
	@Override
	public void severe(String msg) {
		logger.severe(msg);
	}
	@Override
	public void error(Exception e) {
		logger.printStackTrace(e);
	}

}
