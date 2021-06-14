package com.khjxiaogu.TableGames;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Message;

public abstract class Game implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	transient private Group group;
	private long groupId;
	private long RobotId;
	transient private KExecutor scheduler;
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException 
    {
        // perform the default de-serialization first
        aInputStream.defaultReadObject();
        group=MiraiConsole.INSTANCE.getBotOrNull(RobotId).getGroup(groupId);
        // make defensive copy of the mutable Date field
        scheduler=new KExecutor(0,group);
 
        // ensure that object state has not been corrupted or tampered with malicious code
        //validateUserInfo();
    }
 
    /**
     * This is the default implementation of writeObject. Customize as necessary.
     */
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
         
        //ensure that object is in desired state. Possibly run any business rules if applicable.
        //checkUserInfo();
        groupId=group.getId();
        RobotId=group.getBot().getId();
        // perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
        
    }
	/**
	 * @param cplayer  
	 */
	public Game(Group group,int cplayer,int nthread) {
		this.group = group;
		if(nthread>0) {
			scheduler=new KExecutor(nthread,group);
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
	public boolean takeOverMember(long m,Member o) {
		return false;
	}
	public void forceSkip() {
		
	}
	/**
	 * @param ct  
	 */
	public void forceShow(Contact ct) {
	}

	public void forceInterrupt() {
	}

	public boolean specialCommand(Member m, String[] cmds) {
		return false;
	}
}