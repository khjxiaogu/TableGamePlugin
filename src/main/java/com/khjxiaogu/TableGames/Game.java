package com.khjxiaogu.TableGames;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Message;

public abstract class Game {
	private Group group;
	private ExecutorService scheduler;
	/**
	 * @param cplayer  
	 */
	public Game(Group group,int cplayer,int nthread) {
		this.group = group;
		if(nthread>0) {
			scheduler=Executors.newFixedThreadPool(nthread);
		}else scheduler=null;
	}

	public Group getGroup() {
		return group;
	}

	public void forceStop() {
		doFinalize();
	}

	public abstract boolean addMember(Member mem);

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
	public void sendPublicMessage(Message msg) {
		getGroup().sendMessage(msg);
	}
	public void sendPublicMessage(String msg) {
		getGroup().sendMessage(msg);
	}

	public ExecutorService getScheduler() {
		return scheduler;
	}
}