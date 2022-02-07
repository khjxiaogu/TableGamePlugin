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
package com.khjxiaogu.TableGames.utils;

// TODO: Auto-generated Javadoc
/**
 * 标记一个等待中的线程的状态。.
 *
 * @author khjxiaogu
 * file: WaitThread.java
 * time: 2020年10月18日
 */
public class WaitThread {

	/**
	 * 被强制中断时抛出的异常
	 *
	 * @author khjxiaogu
	 * file: WaitThread.java
	 * time: 2020年10月19日
	 */
	public class TerminatedException extends RuntimeException{
		private static final long serialVersionUID = 1L;

	}
	private Thread waiting;
	private boolean terminate=false;
	private Object waitlock=new Object();

	/**
	 * Instantiates a new WaitThread.<br>
	 * 新建一个WaitThread类<br>
	 */
	public WaitThread() {
	}

	/**
	 * 使当前线程等待。<br>.
	 *
	 * @param millis the millis<br>毫秒
	 * @exception TerminatedException 如果terminateWait方法被调用
	 */
	public void startWait(long millis) {
		try {
			synchronized(waitlock){
				waiting=Thread.currentThread();
				terminate=false;
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
		}catch(Throwable T) {}finally {
			synchronized(waitlock) {
				waiting=null;
				if(terminate) {
					terminate=false;
					throw new TerminatedException();
				}
			}
		}
	}

	/**
	 * 停止等待，但是不抛出异常。<br>
	 */
	public void stopWait() {
		synchronized(waitlock) {
			terminate=false;
			if(waiting!=null) {
				Thread td=waiting;
				waiting=null;
				td.interrupt();
			}
		}
	}

	/**
	 * 结束对等待线程的追踪，不停止等待<br>
	 */
	public void endWait() {
		synchronized(waitlock) {
			terminate=false;
			waiting=null;
		}
	}

	/**
	 * 停止等待，抛出异常以便进一步处理。<br>
	 */
	public void terminateWait() {
		synchronized(waitlock) {
			if(waiting!=null) {
				terminate=true;
				Thread td=waiting;
				waiting=null;
				td.interrupt();

			}
		}
	}
}
