package com.khjxiaogu.TableGames;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Message;

public abstract class Game {
	protected final Group group;
	public final ExecutorService scheduler;
	/**
	 * @param cplayer  
	 */
	public Game(Group group,int cplayer,int nthread) {
		this.group = group;
		if(nthread>0) {
			scheduler=Executors.newFixedThreadPool(nthread);
		}else scheduler=null;
	}

	public void forceStop() {
		doFinalize();
	}

	public abstract boolean addMember(Member mem);

	public abstract void forceStart();

	public abstract String getName();
	
	public abstract boolean isAlive();
	protected void doFinalize() {
		if(scheduler!=null) {
			scheduler.shutdownNow();
			try {
				while(!scheduler.awaitTermination(1,TimeUnit.SECONDS)) {
					scheduler.shutdownNow();
				}
			} catch (InterruptedException e) {}
		}
	}
	public void sendPublicMessage(Message msg) {
		group.sendMessage(msg);
	}
	public void sendPublicMessage(String msg) {
		group.sendMessage(msg);
	}
}