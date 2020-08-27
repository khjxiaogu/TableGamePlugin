package com.khjxiaogu.TableGames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;


public class VoteUtil<T extends Player> {
	public Map<T,Integer> voted=new ConcurrentHashMap<>(); 
	public Set<T> tovote=Collections.newSetFromMap(new ConcurrentHashMap<>());
	boolean isEnded=true;
	Thread hintThread;
	int votenum=0;
	int giveups=0;
	public VoteUtil() {
	}
	public void addToVote(T src) {
		synchronized(voted){
			isEnded=false;
			tovote.add(src);
			giveups=0;
			votenum=tovote.size();
		}
	}
	public static void main(String[] args) {
	}
	public void hintVote(ExecutorService scheduler) {
		scheduler.execute(()->{
			hintThread=Thread.currentThread();
			int it=0;
			while(tovote.size()>0&&(!isEnded)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
				it++;
				if(it>=300) {
					it=0;
					for(T p:tovote) {
						p.sendPrivate("请投票！");
					}
				}
			}
		});
	}
	public void giveUp(T p) {
		synchronized(voted){
			if(tovote.remove(p)) {
				if(++giveups*1.0/votenum>0.5) {
					tovote.clear();
				}
			}
		}
	}
	public boolean finished() {
		synchronized(voted){
			return tovote.size()==0;
		}
	}
	public void clear() {
		synchronized(voted){
			isEnded=true;
			votenum=0;
			giveups=0;
			tovote.clear();
			voted.clear();
			if(hintThread!=null) {
				hintThread.interrupt();
				hintThread=null;
			}
		}
	}
	public boolean vote(T src,T id) {
		synchronized(voted){
			if(!tovote.remove(src))return false;
			int vnum=voted.getOrDefault(id,0);
			voted.put(id,vnum+1);
			if(tovote.size()==0) 
				return true;
			return false;
		}
	}
	public void vote(T id) {
		synchronized(voted){
			int vnum=voted.getOrDefault(id,0);
			voted.put(id,vnum+1);
			votenum++;
		}
	}
	public List<T> getMostVoted() {
		synchronized(voted){
			int lastmax=0;
			for(Map.Entry<T,Integer> p:voted.entrySet()) {
				if(p.getValue()>lastmax) {
					lastmax=p.getValue();
				}
			}
			List<T> vpl=new ArrayList<>();
			if(lastmax==1)
				return vpl;
			for(Map.Entry<T,Integer> p:voted.entrySet()) {
				if(p.getValue()==lastmax)
					vpl.add(p.getKey());
			}
			return vpl;
		}
	}
	public List<T> getForceMostVoted() {
		synchronized(voted){
			int lastmax=0;
			for(Map.Entry<T,Integer> p:voted.entrySet()) {
				if(p.getValue()>lastmax) {
					lastmax=p.getValue();
				}
			}
			List<T> vpl=new ArrayList<>();
			for(Map.Entry<T,Integer> p:voted.entrySet()) {
				if(p.getValue()==lastmax)
					vpl.add(p.getKey());
			}
			return vpl;
		}
	}
}
