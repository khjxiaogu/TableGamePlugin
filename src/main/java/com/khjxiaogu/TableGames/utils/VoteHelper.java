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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.khjxiaogu.TableGames.platform.UserFunction;
import com.khjxiaogu.TableGames.platform.UserIdentifier;


public class VoteHelper<T extends UserFunction> {
	public Map<T,Double> voted=new ConcurrentHashMap<>();
	public Set<T> tovote=Collections.newSetFromMap(new ConcurrentHashMap<>());
	boolean isEnded=true;
	Thread hintThread;
	int votenum=0;
	int giveups=0;
	public boolean skipHalf=false;
	public VoteHelper() {
	}
	public void addToVote(T src) {
		synchronized(voted){
			isEnded=false;
			tovote.add(src);
			giveups=0;
			votenum=tovote.size();
		}
	}
	public void hintVote(ExecutorService scheduler) {
		scheduler.execute(()->{
			hintThread=Thread.currentThread();
			int it=0;
			while(tovote.size()>0&&!isEnded) {
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
				if(skipHalf)
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
	public boolean vote(T src,T id,double ticket) {
		synchronized(voted){
			if(!tovote.remove(src))return tovote.size()==0;
			double vnum=voted.getOrDefault(id,0D);
			voted.put(id,vnum+ticket);
			if(tovote.size()==0)
				return true;
			return false;
		}
	}
	public boolean vote(T src,T id) {
		return vote(src,id,1D);
	}
	public void vote(T id) {
		synchronized(voted){
			double vnum=voted.getOrDefault(id,0D);
			voted.put(id,vnum+1);
			votenum++;
		}
	}
	public List<T> getMostVoted() {
		synchronized(voted){
			double lastmax=0;
			for(Map.Entry<T,Double> p:voted.entrySet()) {
				if(p.getValue()>lastmax) {
					lastmax=p.getValue();
				}
			}
			List<T> vpl=new ArrayList<>();
			if(votenum>=4&&lastmax<=1)
				return vpl;
			for(Map.Entry<T,Double> p:voted.entrySet()) {
				if(p.getValue()==lastmax) {
					vpl.add(p.getKey());
				}
			}
			return vpl;
		}
	}
	public List<T> getForceMostVoted() {
		synchronized(voted){
			double lastmax=0;
			for(Map.Entry<T,Double> p:voted.entrySet()) {
				if(p.getValue()>lastmax) {
					lastmax=p.getValue();
				}
			}
			List<T> vpl=new ArrayList<>();
			for(Map.Entry<T,Double> p:voted.entrySet()) {
				if(p.getValue()==lastmax) {
					vpl.add(p.getKey());
				}
			}
			return vpl;
		}
	}
}
