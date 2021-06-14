package com.khjxiaogu.TableGames.depravekill;

import com.khjxiaogu.TableGames.data.GenericPlayerData;
import com.khjxiaogu.TableGames.utils.Utils;

public class WerewolfPlayerData implements GenericPlayerData<WerewolfPlayerData>{
	public int wins;
	public int loses;
	public int winaswolf;
	public int loseaswolf;
	public int winasvill;
	public int loseasvill;
	public int winasgod;
	public int loseasgod;
	public int alive;
	public int dieasvill;
	public int aliveasvill;
	public int dieasgod;
	public int aliveasgod;
	public int dieaswolf;
	public int aliveaswolf;
	public int death;
	public int total;
	public WerewolfPlayerData() {
	}
	public void winAsWolf(boolean isAlive) {
		total++;
		winaswolf++;
		wins++;
		if(isAlive) {
			alive++;
			aliveaswolf++;
		}else {
			death++;
			dieaswolf++;
		}
	}
	public void winAsVill(boolean isAlive) {
		total++;
		winasvill++;
		wins++;
		if(isAlive) {
			alive++;
			aliveasvill++;
		}else {
			death++;
			dieasvill++;
		}
	}
	public void winAsGod(boolean isAlive) {
		total++;
		winasgod++;
		wins++;
		if(isAlive) {
			alive++;
			aliveasgod++;
		}else {
			death++;
			dieasgod++;
		}
	}
	public void loseAsWolf() {
		total++;
		loseaswolf++;
		loses++;
		death++;
		dieaswolf++;
	}
	public void loseAsVill() {
		total++;
		loseasvill++;
		loses++;
		death++;
		dieasvill++;
	}
	public void loseAsGod() {
		total++;
		loseasgod++;
		loses++;
		death++;
		dieasgod++;
	}
	public void win(Fraction frac,boolean isAlive) {
		switch(frac) {
		case Innocent:this.winAsVill(isAlive);break;
		case Wolf:this.winAsWolf(isAlive);break;
		case God:this.winAsGod(isAlive);break;
		}
	}
	public void lose(Fraction frac) {
		switch(frac) {
		case Innocent:this.loseAsVill();break;
		case Wolf:this.loseAsWolf();break;
		case God:this.loseAsGod();break;
		}
	}
	public void log(Fraction frac,Fraction win,boolean isAlive) {
		if(win==Fraction.Innocent&&(frac==Fraction.God||frac==Fraction.Innocent))
			win(frac,isAlive);
		else if(win==Fraction.Wolf&&frac==Fraction.Wolf)
			win(frac,isAlive);
		else
			lose(frac);
	}
	@Override
	public String toString() {
		StringBuilder apd=new StringBuilder();
		apd.append("狼人杀统计数据").append("\n");
		apd.append("总场数 ").append(total).append("\n");
		apd.append("总胜率 ").append(Utils.percent(wins,wins+loses)).append("\n");
		apd.append("神胜率 ").append(Utils.percent(winasgod,winasgod+loseasgod)).append("\n");
		apd.append("民胜率 ").append(Utils.percent(winasvill,winasvill+loseasvill)).append("\n");
		apd.append("狼胜率 ").append(Utils.percent(winaswolf,winaswolf+loseaswolf)).append("\n");
		apd.append("总存活率 ").append(Utils.percent(alive,alive+death)).append("\n");
		apd.append("神存活率 ").append(Utils.percent(aliveasgod,aliveasgod+dieasgod)).append("\n");
		apd.append("民存活率 ").append(Utils.percent(aliveasvill,aliveasvill+dieasvill)).append("\n");
		apd.append("狼存活率 ").append(Utils.percent(aliveaswolf,aliveaswolf+dieaswolf)).append("\n");
		return apd.toString();
	}
	@Override
	public void plus(WerewolfPlayerData another) {
		wins+=another.wins;
		loses+=another.loses;
		winaswolf+=another.winaswolf;
		loseaswolf+=another.loseaswolf;
		winasvill+=another.winasvill;
		loseasvill+=another.loseasvill;
		winasgod+=another.winasgod;
		loseasgod+=another.loseasgod;
		alive+=another.alive;
		dieasvill+=another.dieasvill;
		aliveasvill+=another.aliveasvill;
		dieasgod+=another.dieasgod;
		aliveasgod+=another.aliveasgod;
		dieaswolf+=another.dieaswolf;
		aliveaswolf+=another.aliveaswolf;
		death+=another.death;
		total=Math.max(another.total, total);
	}
}
