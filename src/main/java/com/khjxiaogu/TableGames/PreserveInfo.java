package com.khjxiaogu.TableGames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public abstract class PreserveInfo<T extends Game>{
	Set<Member> topreserve=Collections.newSetFromMap(new ConcurrentHashMap<>());
	Group group;
	Thread td;
	public boolean acceled=false;
	public PreserveInfo(Group g) {
		group=g;
	}
	protected abstract int getSuitMembers();
	protected abstract int getMinMembers();
	protected abstract int getMaxMembers();
	protected abstract Class<T> getGameClass();
	public abstract String getName();
	public static Integer prevSize=0;
	public void addPreserver(Member m) {
		if(topreserve.size()>=getMaxMembers()) {
			group.sendMessage(getName()+"当前已经满人！");
			return;
		}
		if(topreserve.add(m)) {
			m.sendMessage("预定成功");
			group.sendMessage(getName()+"当前预定人数："+topreserve.size()+"\n最低开始人数："+getMinMembers()+"\n欢迎各位预定，格式为“@我 预定"+getName()+"”");
		}else
			m.sendMessage("您已经预定了。");
		synchronized(prevSize) {
			if(topreserve.size()>=getMinMembers()&&prevSize<getMinMembers()) {
				group.sendMessage("已达到最低预定人数，游戏将会在5分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				onStartPending();
			}
			if(topreserve.size()>=getSuitMembers()&&prevSize<getSuitMembers()) {
				group.sendMessage("已达到最佳预定人数，游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				if(!acceled)
					td.interrupt();
				acceled=true;
			}
			prevSize=topreserve.size();
		}
	}
	public void removePreserver(Member m) {
		if(topreserve.remove(m)) {
			m.sendMessage("取消预定成功");
			group.sendMessage("当前预定人数："+topreserve.size()+"最低预定人数"+getMinMembers()+"，欢迎各位预定，格式为“@我 预定"+getName()+"”");
		}else
			m.sendMessage("你还没预定。");
		if(topreserve.size()<getMinMembers()&&td!=null) {
			group.sendMessage("预定人数不足，已取消。");
			td.interrupt();
		}
	}
	public void onStartPending() {
		td=new Thread(()-> {
			try {
				try {
					Thread.sleep(60000);
					Thread.sleep(60000);
					group.sendMessage("游戏将会在3分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
					Thread.sleep(60000);
					Thread.sleep(60000);
					group.sendMessage("游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				} catch (InterruptedException e) {}
				if(topreserve.size()<getMinMembers())return;
				acceled=true;
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
			while(Utils.hasActiveGame(group)) {
				if(topreserve.size()<getMinMembers())return;
				group.sendMessage(getName()+"不能开始，因为有其他游戏正在运行，推迟2分钟……");
				try {Thread.sleep(120000);} catch (InterruptedException e) {}
			}
			if(topreserve.size()<getMinMembers())return;
			td=null;
			
			startGame();
			acceled=false;
		});
		td.start();
	}
	public void startNow() {
		if(td!=null) {
			if(!acceled) {
				td.interrupt();
				group.sendMessage("游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
			}
			group.sendMessage("游戏已经进入1分钟等待！");
			acceled=true;
		}else
			startGame();
	}
	public boolean startGame() {
		if(topreserve.isEmpty())return false;
		topreserve.removeIf(m->Utils.hasMember(m.getId()));
		Game gm=Utils.createGame(getGameClass(),group,topreserve.size());
		List<Member> mems=new ArrayList<>(topreserve);
		topreserve.clear();
		Collections.shuffle(mems);
		mems.removeIf(m->gm.addMember(m));
		return true;
	}
}
