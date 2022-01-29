package com.khjxiaogu.TableGames.platform.mirai;

import java.security.SecureRandom;

public class SlowUtils {

	public SlowUtils() {
	}
	private static final Object slowLock=new Object();
	private static final SecureRandom r=new SecureRandom();
	public static void runSlowly(Runnable obj) {
		synchronized(slowLock) {
			obj.run();
			try {
				Thread.sleep(r.nextInt(300));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
