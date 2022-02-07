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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;


public abstract class PreserveInfo<T extends Game>{
	Map<AbstractUser,Long> topreserve=new ConcurrentHashMap<>();
	AbstractRoom group;
	Map<String, String> args=null;
	static class UserItem{
		AbstractUser user;
		String item;
		public UserItem(String item, AbstractUser user) {
			this.item = item;
			this.user = user;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = ((item == null) ? 0 : item.hashCode());
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			UserItem other = (UserItem) obj;
			if (item == null) {
				if (other.item != null)
					return false;
			} else if (!item.equals(other.item))
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}
	}
	Map<UserItem,String> userset=new ConcurrentHashMap<>();
	Thread td;
	public boolean acceled=false;
	public PreserveInfo(AbstractRoom g) {
		group=g;
		notificationService.scheduleAtFixedRate(new TimerTask() {@Override
			public void run() {decreaseTimer();}},1000,1000);
	}
	protected abstract int getSuitMembers();
	protected abstract int getMinMembers();
	protected abstract int getMaxMembers();
	protected abstract GameCreater<T> getGameClass();
	private Timer notificationService=new Timer();
	public static int KeepAlive=1800;
	public int AliveCounter=0;
	public abstract String getName();
	public boolean enablefake=false;
	public static Integer prevSize=0;
	public final boolean addConfig(AbstractUser ar,String item,String set) {
		if(isAvailableConfig(ar,item,set)) {
			userset.put(new UserItem(item,ar),set);
			return true;
		}
		return false;
	}
	protected boolean isAvailableConfig(AbstractUser ar,String item,String set) {
		return false;
	}
	public int getCurrentNum() {
		int crn=topreserve.size();
		if(enablefake&&crn<getMaxMembers()-3) {
			crn+=3;
		}
		return crn;
	}
	public int getActualCurrentNum() {
		return topreserve.size();
	}
	private void decreaseTimer() {
		if(topreserve.isEmpty())return;
		AliveCounter--;
		if(AliveCounter==0) {
			for(AbstractUser m:topreserve.keySet()) {
				m.sendPrivate("由于超时未能开始，"+m.getMemberString()+" 的预定已被取消。");
			}
			this.removeAll();
		}
	}
	public boolean hasPreserver(AbstractUser m) {
		return topreserve.containsKey(m);
	}
	public void addPreserver(AbstractUser m) {
		if(getCurrentNum()>=getMaxMembers()) {
			group.sendMessage(getName()+"当前已经满人！");
			return;
		}
		/*
		try {
			m.getBot().getFriend(m.getId());
		}catch(Exception e) {
			m.sendPrivate("为了保证游戏流畅进行，请加我好友。");
		}
		 */
		AliveCounter=PreserveInfo.KeepAlive;
		if(topreserve.put(m,new Date().getTime()) == null) {
			m.sendPrivate(m.getMemberString()+" 预定"+this.getName()+"成功");
			sendPersonInfo();
		} else {
			m.sendPrivate(m.getMemberString()+"您已经预定了。");
		}
		synchronized(PreserveInfo.prevSize) {
			if(getActualCurrentNum()>=getSuitMembers()&&PreserveInfo.prevSize<getSuitMembers()) {
				group.sendMessage("已达到最佳预定人数，游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“##预定"+getName()+"”");
				if(!acceled) {
					td.interrupt();
				}
				acceled=true;
			}else
			if(getCurrentNum()>=getMinMembers()&&PreserveInfo.prevSize<getMinMembers()) {
				group.sendMessage("已达到最低预定人数，游戏将会在5分钟后开始，还想参加的请抓紧时间预定，格式为“##预定"+getName()+"”");
				if(getActualCurrentNum()>=getMinMembers()) {
					onStartPending();
				} else
					return;
			}
			PreserveInfo.prevSize=getActualCurrentNum();
		}
	}
	public void sendPersonInfo() {
		if(getCurrentNum()<getMinMembers())
			group.sendMessage(getName()+"当前预定人数："+getCurrentNum()+"\n最低开始人数："+getMinMembers()+"\n欢迎预定，格式为“##预定"+getName()+"”\n想玩的都可以预定，本预定会在30分钟后自动取消。");
		else
			group.sendMessage(getName()+"当前预定人数："+getCurrentNum()+"\n最低开始人数："+getMinMembers()+"\n欢迎预定，格式为“##预定"+getName()+"”。");
	}
	@SuppressWarnings("deprecation")
	public void removeAll() {
		topreserve.clear();
		if(td!=null) {
			td.stop();
			td=null;
		}
	}
	public void notifyPreserver() {
		for(AbstractUser m:topreserve.keySet()) {
			m.sendPrivate(this.getName()+"游戏即将开始，请注意！");
		}
	}
	public String getPreserveList() {
		StringBuilder sb=new StringBuilder(this.getName());
		sb.append("预定列表：\n");
		for(AbstractUser m:topreserve.keySet()) {
			sb.append(m.getNameCard()).append("\n");
		}
		return sb.toString();
	}
	public static Map<String, String> queryToMap(String query) {
		Map<String, String> result = new HashMap<>();
		if (query == null || query.length() <= 0)
			return result;

		for (String param : query.split("&")) {
			String[] entry = param.split("=");
			if (entry.length > 1) {
				result.put(entry[0], entry[1]);
			} else {
				result.put(entry[0], "");
			}
		}
		return result;
	}
	public void removePreserver(AbstractUser m) {
		removePreserver(m,false);
	}
	public void removePreserver(AbstractUser m,boolean force) {
		Long crn=topreserve.get(m);
		if(crn != null)
		{
			if(force||new Date().getTime()-crn>180000) {
				m.sendPrivate("取消预定成功");
				topreserve.remove(m);
				sendPersonInfo();
			} else {
				m.sendPrivate("预定后的三分钟内不能取消预定！");
			}
		} else {
			m.sendPrivate("你还没预定。");
		}
		if(getActualCurrentNum()<getMinMembers()&&td!=null) {
			group.sendMessage("预定人数不足，已取消。");
			PreserveInfo.prevSize=getActualCurrentNum();
			td.interrupt();
			td=null;
		}
	}

	public void onStartPending() {
		td=new Thread(()-> {
			acceled=false;
			try {
				try {
					Thread.sleep(60000);
					Thread.sleep(60000);
					group.sendMessage("游戏将会在3分钟后开始，还想参加的请抓紧时间预定，格式为“##预定"+getName()+"”");
					Thread.sleep(60000);
					Thread.sleep(60000);
					group.sendMessage("游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“##预定"+getName()+"”");
					notifyPreserver();
				} catch (InterruptedException e) {}
				if(getActualCurrentNum()<getMinMembers())return;
				acceled=true;
				Thread.sleep(60000);
			} catch (InterruptedException e) {}
			if(GameUtils.hasActiveGame(group)) {
				group.sendMessage(getName()+"不能开始，因为有其他游戏正在运行，将在本次游戏结束后两分钟内开始……");
			}
			while(GameUtils.hasActiveGame(group)) {
				if(getActualCurrentNum()<getMinMembers())return;

				try {Thread.sleep(120000);} catch (InterruptedException e) {}
			}
			if(getActualCurrentNum()<getMinMembers())return;
			td=null;

			startGame();
			AliveCounter=0;
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
		} else {
			startGame();
		}
	}
	@SuppressWarnings("deprecation")
	public void startForce() {
		if(td!=null) {
			acceled=true;
			td.stop();
			td=null;
		}
		startGame();
	}
	public String getArgs() {
		StringBuilder sb=new StringBuilder(getName()).append("游戏参数：\n");
		if(args!=null) {
			for(Map.Entry<String,String> mes:args.entrySet()) {
				sb.append(mes.getKey()).append(":").append(mes.getValue()).append("\n");
			}
		}
		return sb.toString();
	}
	public void setArgs(String[] val) {
		args=PreserveInfo.queryToMap(String.join(" ",val));
	}
	public void clearArgs() {
		args=null;
	}
	public boolean startGame() {
		if(topreserve.isEmpty())return false;
		System.out.println("starting game");
		this.group.sendMessage("尝试开始游戏中...");
		topreserve.keySet().removeIf(m->GameUtils.hasMember(m.getId()));
		Game gm;
		if(args==null) {
			gm=GameUtils.createGame(getGameClass(),group,topreserve.size());
		} else {
			gm=GameUtils.createGame(getGameClass(),group,topreserve.size(),args);
		}
		for(Entry<UserItem, String> me:userset.entrySet()) {
			gm.userSettings(me.getKey().user,me.getKey().item,me.getValue());
		}
		userset.clear();
		args=null;
		List<AbstractUser> mems=new ArrayList<>(topreserve.keySet());
		topreserve.clear();
		Collections.shuffle(mems);
		Collections.reverse(mems);
		mems.removeIf(m->gm.addMember(m));
		this.group.sendMessage("游戏已经开始...");
		return true;
	}
}
