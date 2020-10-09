package com.khjxiaogu.TableGames.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.werewolf.Villager;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public abstract class PreserveInfo<T extends Game>{
	Set<Member> topreserve=Collections.newSetFromMap(new ConcurrentHashMap<>());
	Group group;
	Map<String, String> args=null;
	Thread td;
	public boolean acceled=false;
	public PreserveInfo(Group g) {
		group=g;
	}
	static Map<Long,Class<? extends Villager>> lastJobs=new HashMap<>();
	protected abstract int getSuitMembers();
	protected abstract int getMinMembers();
	protected abstract int getMaxMembers();
	protected abstract Class<T> getGameClass();
	public abstract String getName();
	public boolean enablefake=false;
	public static Integer prevSize=0;
	public int getCurrentNum() {
		int crn=topreserve.size();
		if(enablefake&&crn<getMaxMembers()-3) {
			crn+=3;
		}
		return crn;
	};
	public int getActualCurrentNum() {
		return topreserve.size();
	};
	public void addPreserver(Member m) {
		if(getCurrentNum()>=getMaxMembers()) {
			group.sendMessage(getName()+"当前已经满人！");
			return;
		}
		try {
			m.getBot().getFriend(m.getId());
		}catch(Exception e) {
			m.sendMessage("为了保证游戏流畅进行，请加我好友。");
		}
		if(topreserve.add(m)) {
			m.sendMessage("预定成功");
			group.sendMessage(getName()+"当前预定人数："+getCurrentNum()+"\n最低开始人数："+getMinMembers()+"\n欢迎各位预定，格式为“@我 预定"+getName()+"”");
		}else
			m.sendMessage("您已经预定了。");
		synchronized(prevSize) {
			if(getCurrentNum()>=getMinMembers()&&prevSize<getMinMembers()) {
				group.sendMessage("已达到最低预定人数，游戏将会在5分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				if(getActualCurrentNum()>=getMinMembers())
					onStartPending();
				else
					return;
			}
			if(getActualCurrentNum()>=getSuitMembers()&&prevSize<getSuitMembers()) {
				group.sendMessage("已达到最佳预定人数，游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				if(!acceled)
					td.interrupt();
				acceled=true;
			}
			prevSize=getActualCurrentNum();
		}
	}
	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<>();
		if (query == null || query.length() <= 0)
			return result;

		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1)
				result.put(entry[0], entry[1]);
			else
				result.put(entry[0], "");
		}
		return result;
	}
	public void removePreserver(Member m) {
		if(topreserve.remove(m)) {
			m.sendMessage("取消预定成功");
			group.sendMessage("当前预定人数："+getCurrentNum()+"最低预定人数"+getMinMembers()+"，欢迎各位预定，格式为“@我 预定"+getName()+"”");
		}else
			m.sendMessage("你还没预定。");
		if(getActualCurrentNum()<getMinMembers()&&td!=null) {
			group.sendMessage("预定人数不足，已取消。");
			prevSize=getActualCurrentNum();
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
				if(getActualCurrentNum()<getMinMembers())return;
				acceled=true;
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
			if(Utils.hasActiveGame(group))
				group.sendMessage(getName()+"不能开始，因为有其他游戏正在运行，将在本次游戏结束后两分钟内开始……");
			while(Utils.hasActiveGame(group)) {
				if(getActualCurrentNum()<getMinMembers())return;
				
				try {Thread.sleep(120000);} catch (InterruptedException e) {}
			}
			if(getActualCurrentNum()<getMinMembers())return;
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
	public void setArgs(String[] val) {
		args=queryToMap(String.join(" ",val));
	}
	public boolean startGame() {
		if(topreserve.isEmpty())return false;
		System.out.println("starting game");
		this.group.sendMessage("尝试开始游戏中...");
		topreserve.removeIf(m->Utils.hasMember(m.getId()));
		Game gm;
		if(args==null)
			gm=Utils.createGame(getGameClass(),group,topreserve.size());
		else
			gm=Utils.createGame(getGameClass(),group,topreserve.size(),args);
		args=null;
		List<Member> mems=new ArrayList<>(topreserve);
		topreserve.clear();
		Collections.shuffle(mems);
		Collections.reverse(mems);
		mems.removeIf(m->gm.addMember(m));
		this.group.sendMessage("游戏已经开始...");
		return true;
	}
}
