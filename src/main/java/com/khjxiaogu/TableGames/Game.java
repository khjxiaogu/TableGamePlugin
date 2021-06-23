package com.khjxiaogu.TableGames;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.message.IMessage;


public abstract class Game implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private AbstractRoom group;
	transient private KExecutor scheduler;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		// perform the default de-serialization first
		aInputStream.defaultReadObject();
		// make defensive copy of the mutable Date field
		scheduler=new KExecutor(0,group);

		// ensure that object state has not been corrupted or tampered with malicious code
		//validateUserInfo();
	}

	/**
	 * This is the default implementation of writeObject. Customize as necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();

	}
	/**
	 * @param cplayer
	 */
	public Game(AbstractRoom group,int cplayer,int nthread) {
		this.group = group;
		if(nthread>0) {
			scheduler=new KExecutor(nthread,group);
		} else {
			scheduler=null;
		}
	}

	public AbstractRoom getGroup() {
		return group;
	}

	public void forceStop() {
		doFinalize();
	}

	public abstract void forceStart();

	public abstract String getName();

	public abstract boolean isAlive();
	protected void doFinalize() {
		if(getScheduler()!=null) {
			getScheduler().shutdownNow();
			try {
				while(!getScheduler().awaitTermination(1,TimeUnit.SECONDS)) {
					getScheduler().shutdownNow();
				}
			} catch (InterruptedException e) {}
		}
	}
	public abstract boolean onReAttach(Long id);
	public void sendPublicMessage(IMessage msg) {
		try {
			getGroup().sendMessage(msg);
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					getGroup().sendMessage(msg);
					return;
				}catch(Exception ex2) {}
			}
		}

	}
	public void sendPublicMessage(String msg) {
		try {
			getGroup().sendMessage(msg);
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					getGroup().sendMessage(msg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	public KExecutor getScheduler() {
		return scheduler;
	}
	public void forceSkip() {

	}
	/**
	 * @param ct
	 */
	public void forceShow(AbstractPlayer ct) {
	}

	public void forceInterrupt() {
	}



	public abstract boolean addMember(AbstractPlayer mem);

	/**
	 * @param id  
	 * @param o 
	 */
	public boolean takeOverMember(long id, AbstractPlayer o) {
		return false;
	}

	/**
	 * @param m  
	 * @param cmds 
	 */
	public boolean specialCommand(AbstractPlayer m, String[] cmds) {
		return false;
	}
}