package com.khjxiaogu.TableGames.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PlayerCredit {
	Map<String,Integer> items=new ConcurrentHashMap<>();
	double point=0;
	Boolean changed=false;
	int unusedsince=0;
	public PlayerCredit() {
		
	}
	public void load(JsonObject jo) {
		synchronized (changed) {
			if(!jo.has("point"))return;
			point=jo.get("point").getAsInt();
			items.clear();
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
			for(Map.Entry<String,Integer> je:items.entrySet()) {
				its.addProperty(je.getKey(),je.getValue());
			}
			changed=false;
			return jo;
		}
	}
	public static double normalizedb(double d) {
		return Math.round(d*100)/100;
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
			if(crn>0)
				items.put(name,crn);
			else
				items.remove(name);
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
			if(crn>0)
				items.put(name,crn);
			else
				items.remove(name);
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
			return normalizedb(point);
		}
	}
	public double removePT(double count){
		synchronized (changed) {
			point-=count;
			changed=true;
			unusedsince=0;
			return normalizedb(point);
		}
	}
	public double withdrawPT(double count){
		synchronized (changed) {
			if(point<count)return normalizedb(point-count);
			point-=count;
			changed=true;
			unusedsince=0;
			return normalizedb(point);
		}
	}
	public double getPT() {
		synchronized (changed) {
			unusedsince=0;
			return normalizedb(point);
		}
	}
	public String toString() {
		StringBuilder sb=new StringBuilder();
		sb.append("当前积分：").append(point);
		sb.append("\n持有物品：\n");
		if(items.isEmpty())
			sb.append("空");
		else
			synchronized (changed) {
				for(Map.Entry<String,Integer> je:items.entrySet()) {
					sb.append(je.getKey()).append(" x").append(je.getValue());
				}
			}
		return sb.toString();
	}
}
