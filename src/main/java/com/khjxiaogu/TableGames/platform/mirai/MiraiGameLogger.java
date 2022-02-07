/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
