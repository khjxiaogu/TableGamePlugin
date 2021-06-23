package com.khjxiaogu.TableGames.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.AbstractRoom;


public abstract class PreserveInfo<T extends Game>{
	Map<AbstractPlayer,Long> topreserve=new ConcurrentHashMap<>();
	AbstractRoom group;
	Map<String, String> args=null;
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
	protected abstract Class<T> getGameClass();
	private Timer notificationService=new Timer();
	public static int KeepAlive=1800;
	public int AliveCounter=0;
	public abstract String getName();
	public boolean enablefake=false;
	public static Integer prevSize=0;
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
			for(AbstractPlayer m:topreserve.keySet()) {
				m.sendPrivate("由于超时未能开始，您的预定已被取消。");
			}
			this.removeAll();
		}
	}
	public void addPreserver(AbstractPlayer m) {
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
			m.sendPrivate("预定成功");
			group.sendMessage(getName()+"当前预定人数："+getCurrentNum()+"\n最低开始人数："+getMinMembers()+"\n欢迎各位预定，格式为“@我 预定"+getName()+"”");
		} else {
			m.sendPrivate("您已经预定了。");
		}
		synchronized(PreserveInfo.prevSize) {
			if(getCurrentNum()>=getMinMembers()&&PreserveInfo.prevSize<getMinMembers()) {
				group.sendMessage("已达到最低预定人数，游戏将会在5分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				if(getActualCurrentNum()>=getMinMembers()) {
					onStartPending();
				} else
					return;
			}
			if(getActualCurrentNum()>=getSuitMembers()&&PreserveInfo.prevSize<getSuitMembers()) {
				group.sendMessage("已达到最佳预定人数，游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
				if(!acceled) {
					td.interrupt();
				}
				acceled=true;
			}
			PreserveInfo.prevSize=getActualCurrentNum();
		}
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
		for(AbstractPlayer m:topreserve.keySet()) {
			m.sendPrivate(this.getName()+"游戏即将开始，请注意！");
		}
	}
	public String getPreserveList() {
		StringBuilder sb=new StringBuilder(this.getName());
		sb.append("预定列表：\n");
		for(AbstractPlayer m:topreserve.keySet()) {
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
	public void removePreserver(AbstractPlayer m) {
		removePreserver(m,false);
	}
	public void removePreserver(AbstractPlayer m,boolean force) {
		Long crn=topreserve.get(m);
		if(crn != null)
		{
			if(force||new Date().getTime()-crn>180000) {
				m.sendPrivate("取消预定成功");
				topreserve.remove(m);
				group.sendMessage("当前预定人数："+getCurrentNum()+"最低预定人数"+getMinMembers()+"，欢迎各位预定，格式为“@我 预定"+getName()+"”");
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
					group.sendMessage("游戏将会在3分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
					Thread.sleep(60000);
					Thread.sleep(60000);
					group.sendMessage("游戏将会在1分钟后开始，还想参加的请抓紧时间预定，格式为“@我 预定"+getName()+"”");
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
		args=null;
		List<AbstractPlayer> mems=new ArrayList<>(topreserve.keySet());
		topreserve.clear();
		Collections.shuffle(mems);
		Collections.reverse(mems);
		mems.removeIf(m->gm.addMember(m));
		this.group.sendMessage("游戏已经开始...");
		return true;
	}
}
