package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.UnifiedLogger;

import net.mamoe.mirai.utils.MiraiLogger;

public class MiraiGameLogger implements UnifiedLogger {
	MiraiLogger logger;
	@Override
	public void debug(String msg) {
		logger.debug(msg);
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	public MiraiGameLogger(MiraiLogger logger) {
		this.logger = logger;
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
		logger.error(msg);
	}

	@Override
	public void error(Exception e) {
		logger.error(e);
	}

}
