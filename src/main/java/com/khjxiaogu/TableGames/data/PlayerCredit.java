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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.khjxiaogu.TableGames.utils.TimeUtil;

public class PlayerCredit {
	Map<String,Integer> items=new ConcurrentHashMap<>();
	double point=0;
	Boolean changed=false;
	int unusedsince=0;
	long canPlayIn=0;
	public PlayerCredit() {

	}
	public void load(JsonObject jo) {
		synchronized (changed) {
			if(!jo.has("point"))return;
			point=jo.get("point").getAsInt();
			items.clear();
			if(jo.has("baned"))
			canPlayIn=jo.get("baned").getAsLong();
			JsonObject jx=jo.get("items").getAsJsonObject();
			for(Map.Entry<String,JsonElement> je:jx.entrySet()) {
				items.put(je.getKey(),je.getValue().getAsInt());
			}
		}
	}
	public JsonObject save() {
		synchronized (changed) {
			JsonObject jo=new JsonObject();
			jo.addProperty("point",point);
			JsonObject its=new JsonObject();
			jo.add("items",its);
			jo.addProperty("baned", canPlayIn);
			for(Map.Entry<String,Integer> je:items.entrySet()) {
				its.addProperty(je.getKey(),je.getValue());
			}
			changed=false;
			return jo;
		}
	}
	public void addBan(long time) {
		assumeChange();
		long now=TimeUtil.getTime();
		if(canPlayIn>now) 
			canPlayIn+=time;
		else
			canPlayIn=time+now;
	}
	public long isBanned() {
		long now=TimeUtil.getTime();
		if(canPlayIn>now)
			return canPlayIn;
		return 0;
	}
	public static double normalizedb(double d) {
		return Math.round(d*100)/100D;
	}
	public boolean hasChange() {
		return changed;
	}
	public void assumeChange() {
		changed=true;
	}
	public int giveItem(String name,int count) {
		synchronized (changed) {
			int crn=items.getOrDefault(name,0);
			crn+=count;
			items.put(name,crn);
			changed=true;
			unusedsince=0;
			return crn;
		}
	}
	public int removeItem(String name,int count){
		synchronized (changed) {
			int crn=items.getOrDefault(name,0);
			crn-=count;
			if(crn>0) {
				items.put(name,crn);
			} else {
				items.remove(name);
			}
			changed=true;
			unusedsince=0;
			return crn;
		}
	}
	public int withdrawItem(String name,int count){
		synchronized (changed) {
			int crn=items.getOrDefault(name,0);
			if(crn<count)return crn-count;
			crn-=count;
			if(crn>0) {
				items.put(name,crn);
			} else {
				items.remove(name);
			}
			changed=true;
			unusedsince=0;
			return crn;
		}
	}
	public int getItem(String name) {
		synchronized (changed) {
			unusedsince=0;
			return items.getOrDefault(name,0);
		}
	}
	public double givePT(double count) {
		synchronized (changed) {
			point+=count;
			changed=true;
			unusedsince=0;
			return PlayerCredit.normalizedb(point);
		}
	}
	public double removePT(double count){
		synchronized (changed) {
			point-=count;
			changed=true;
			unusedsince=0;
			return PlayerCredit.normalizedb(point);
		}
	}
	public double withdrawPT(double count){
		synchronized (changed) {
			if(point<count)return PlayerCredit.normalizedb(point-count);
			point-=count;
			changed=true;
			unusedsince=0;
			return PlayerCredit.normalizedb(point);
		}
	}
	public double getPT() {
		synchronized (changed) {
			unusedsince=0;
			return PlayerCredit.normalizedb(point);
		}
	}
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("当前积分：").append(normalizedb(point));
		sb.append("\n持有物品：\n");
		if(items.isEmpty()) {
			sb.append("空");
		} else {
			synchronized (changed) {
				for(Map.Entry<String,Integer> je:items.entrySet()) {
					sb.append(je.getKey()).append(" x").append(je.getValue()).append("\n");
				}
			}
		}
		return sb.toString();
	}
	public void removeBan() {
		assumeChange();
		canPlayIn=0;
	}
	public boolean hasItem(String name) {
		return items.getOrDefault(name,0)>0;
	}
}
