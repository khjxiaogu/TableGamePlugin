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

import java.security.SecureRandom;

public class SlowUtils {

	public SlowUtils() {
	}
	private static final Object slowLock=new Object();
	private static final SecureRandom r=new SecureRandom();
	public static void runSlowly(Runnable obj) {
		synchronized(slowLock) {
			try {
				Thread.sleep(r.nextInt(500));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			obj.run();
			
		}
	}
}
