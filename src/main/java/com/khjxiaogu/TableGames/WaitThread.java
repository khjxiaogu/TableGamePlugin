package com.khjxiaogu.TableGames;

public class WaitThread {
	Thread waiting;
	boolean terminate=false;
	Object waitlock=new Object();
	public WaitThread() {
	}
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
					throw new RuntimeException();
				}
			}
		}
	}
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
	public void endWait() {
		synchronized(waitlock) {
			terminate=false;
			waiting=null;
		}
	}
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
