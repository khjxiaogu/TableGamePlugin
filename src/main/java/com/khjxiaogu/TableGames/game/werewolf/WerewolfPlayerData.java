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
package com.khjxiaogu.TableGames.game.werewolf;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.khjxiaogu.TableGames.data.application.GenericPlayerData;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.Role;
import com.khjxiaogu.TableGames.utils.Utils;

public class WerewolfPlayerData implements GenericPlayerData<WerewolfPlayerData> {
	public static class RoleWinInfo implements Cloneable{
		public int wins;
		public int loses;
		public int total;
		public int alive;
		public int death;
		public double saccuracy;
		public long saccuracydemon;
		public double vaccuracy;
		public long vaccuracydemon;

		public RoleWinInfo() {
		}

		public RoleWinInfo(Role r) {
		}

		public void winAsRole(boolean alive, double sacc, long sad, double vacc, long vad) {
			if (alive)
				this.alive++;
			else
				this.death++;
			wins++;
			total++;
			saccuracy += sacc;
			saccuracydemon += sad;
			vaccuracy += vacc;
			vaccuracydemon += vad;
		}

		public void loseAsRole(double sacc, long sad, double vacc, long vad) {
			death++;
			loses++;
			total++;
			saccuracy += sacc;
			saccuracydemon += sad;
			vaccuracy += vacc;
			vaccuracydemon += vad;
		}
		public RoleWinInfo add(RoleWinInfo other) {
			wins+=other.wins;
			loses+=other.loses;
			total+=other.total;
			alive+=other.alive;
			death+=other.death;
			saccuracy+=other.saccuracy;
			saccuracydemon+=other.saccuracydemon;
			vaccuracy+=other.vaccuracy;
			vaccuracydemon+=other.vaccuracydemon;
			return this;
		}
	}

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
	public double vaccuracy;
	public long vaccuracydemon;
	public double saccuracy;
	public long saccuracydemon;
	public Map<Role, RoleWinInfo> winsrole = new EnumMap<>(Role.class);
	public List<String> lastTen=new ArrayList<>();
	public WerewolfPlayerData() {

	}

	public void winAsRole(Role r, boolean alive, double acc, long ad, double sacc, long sad) {
		if (r == null)
			return;
		winsrole.computeIfAbsent(r, RoleWinInfo::new).winAsRole(alive, acc, ad, sacc, sad);
	}

	public void loseAsRole(Role r, double acc, long ad, double sacc, long sad) {
		if (r == null)
			return;
		winsrole.computeIfAbsent(r, RoleWinInfo::new).loseAsRole(acc, ad, sacc, sad);
	}

	public void winAsWolf(boolean isAlive) {
		total++;
		winaswolf++;
		wins++;
		if (isAlive) {
			alive++;
			aliveaswolf++;
		} else {
			death++;
			dieaswolf++;
		}
	}

	public void winAsVill(boolean isAlive) {
		total++;
		winasvill++;
		wins++;
		if (isAlive) {
			alive++;
			aliveasvill++;
		} else {
			death++;
			dieasvill++;
		}
	}

	public void winAsGod(boolean isAlive) {
		total++;
		winasgod++;
		wins++;
		if (isAlive) {
			alive++;
			aliveasgod++;
		} else {
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

	public void win(Villager p, Fraction frac, boolean isAlive, double acc, long cd, double vacc, long vcd) {
		this.winAsRole(Role.getRole(p), isAlive, acc, cd, vacc, vcd);
		populateRank(p.getRole()+" "+(isAlive?"存活":"胜利"));
		switch (frac) {
		case Innocent:
			winAsVill(isAlive);
			break;
		case Wolf:
			winAsWolf(isAlive);
			break;
		case God:
			winAsGod(isAlive);
			break;
		}
	}
	public void populateRank(String s) {
		if(lastTen==null)
			lastTen=new ArrayList<>();
		while(lastTen.size()>=10) {
			lastTen.remove(0);
		}
		lastTen.add(s);	
	}
	public void lose(Villager p, Fraction frac, double acc, long cd, double vacc, long vcd) {
		this.loseAsRole(Role.getRole(p), acc, cd, vacc, vcd);
		populateRank(p.getRole()+" 失败");
		switch (frac) {
		case Innocent:
			loseAsVill();
			break;
		case Wolf:
			loseAsWolf();
			break;
		case God:
			loseAsGod();
			break;
		}
	}

	public boolean log(Villager player, Fraction win, boolean isAlive) {
		Fraction frac = player.getRealFraction();
		if (win == Fraction.Innocent && (frac == Fraction.God || frac == Fraction.Innocent)) {

			saccuracydemon += player.skilled;
			vaccuracydemon += player.voted;
			saccuracy += player.skillAccuracy + 0.25 * player.skilled;
			vaccuracy += player.voteAccuracy + 0.25 * player.voted;
			win(player, frac, isAlive, player.skillAccuracy + 0.25 * player.skilled, player.skilled,
					player.voteAccuracy + 0.25 * player.voted, player.voted);
			return true;
		} else if (win == Fraction.Wolf && frac == Fraction.Wolf) {
			win(player, frac, isAlive, 0, 0, 0, 0);
			return true;
		} else {
			if (frac != Fraction.Wolf) {
				saccuracydemon += player.skilled;
				vaccuracydemon += player.voted;
				saccuracy += player.skillAccuracy - (player.skilled > 0 ? 0.1 : 0);
				vaccuracy += player.voteAccuracy - (player.voted > 0 ? 0.1 : 0);
				lose(player, frac, player.skillAccuracy + 0.25 * player.skilled, player.skilled,
						player.voteAccuracy + 0.25 * player.voted, player.voted);
			} else
				lose(player, frac, 0, 0, 0, 0);
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder apd = new StringBuilder();
		apd.append("狼人杀统计数据").append("\n");
		apd.append("总场数 ").append(total).append("\n");
		apd.append("总胜率 ").append(Utils.percent(wins, wins + loses)).append("\n");
		apd.append("神胜率 ").append(Utils.percent(winasgod, winasgod + loseasgod)).append("\n");
		apd.append("民胜率 ").append(Utils.percent(winasvill, winasvill + loseasvill)).append("\n");
		apd.append("狼胜率 ").append(Utils.percent(winaswolf, winaswolf + loseaswolf)).append("\n");
		apd.append("总存活率 ").append(Utils.percent(alive, alive + death)).append("\n");
		apd.append("神存活率 ").append(Utils.percent(aliveasgod, aliveasgod + dieasgod)).append("\n");
		apd.append("民存活率 ").append(Utils.percent(aliveasvill, aliveasvill + dieasvill)).append("\n");
		apd.append("狼存活率 ").append(Utils.percent(aliveaswolf, aliveaswolf + dieaswolf)).append("\n");
		apd.append("综合准确率：").append(Utils.percent(saccuracy * 2 + vaccuracy, saccuracydemon * 2 + vaccuracydemon)).append("\n");
		apd.append("输入##狼人杀分析 <角色/阵营> 查看角色统计").append("\n");
		apd.append("输入##狼人杀分析 战绩 查看最近战绩");
		return apd.toString();
	}

	@Override
	public String getStatistic(String v) {
		if(v.equals("战绩")) {
			if(lastTen==null||lastTen.isEmpty())
				return " 暂无数据";
			StringBuilder apd = new StringBuilder(" 最近十场战绩\n");
			
			for(String s:lastTen) {
				apd.append(s).append("\n");
			}
			return apd.toString();
		}
		if(v.equals("全部")) {
			StringBuilder apd = new StringBuilder(" 全部职业分析");
			for(Entry<Role, RoleWinInfo> rwi:winsrole.entrySet()) {
				apd.append("\n").append(rwi.getKey().getName());
				apd.append(" 胜率：" + Utils.percent(rwi.getValue().wins, rwi.getValue().total));
				apd.append(" 存活率：" + Utils.percent(rwi.getValue().alive, rwi.getValue().total));
				if(rwi.getKey().getFraction()!=Fraction.Wolf)
					apd.append(" 准确率：" +Utils.percent(rwi.getValue().saccuracy * 2 + rwi.getValue().vaccuracy, rwi.getValue().saccuracydemon * 2 + rwi.getValue().vaccuracydemon));
			}
			return apd.toString();
		}else if(v.equals("概率")) {
			StringBuilder apd = new StringBuilder(" 职业出现频率");
			int tot=0;
			for(Entry<Role, RoleWinInfo> rwi:winsrole.entrySet())
				tot+=rwi.getValue().total;
			for(Entry<Role, RoleWinInfo> rwi:winsrole.entrySet()) {
				apd.append("\n").append(rwi.getKey().getName());
				apd.append(" 频率：" + Utils.percentDot2(rwi.getValue().total,tot));

			}
			return apd.toString();
		}
		Fraction f=Fraction.getByName(v);
		Role r = Role.getByNameOrNull(v);
		
		if(r!=null) {
			RoleWinInfo rwi = winsrole.get(r);
			if (rwi == null)
				return " 暂无数据";
			StringBuilder apd = new StringBuilder();
			apd.append(" "+r.getName() + "统计").append("\n");
			apd.append("胜率：" + Utils.percent(rwi.wins, rwi.total)).append("\n");
			apd.append("存活率：" + Utils.percent(rwi.alive, rwi.total)).append("\n");
			if(r.getFraction()!=Fraction.Wolf)
				apd.append("准确率：" +Utils.percent(rwi.saccuracy * 2 + rwi.vaccuracy, rwi.saccuracydemon * 2 + rwi.vaccuracydemon));
			return apd.toString();
		}
		if(f!=null) {
			final RoleWinInfo rwi=new RoleWinInfo();
			for(Role rx:Role.values()) {
				if(rx.getFraction()==f) {
					RoleWinInfo rwin=winsrole.get(rx);
					if(rwin!=null)rwi.add(rwin);
				}
			}
			StringBuilder apd = new StringBuilder();
			apd.append(" "+f.name+ "统计").append("\n");
			apd.append("胜率：" + Utils.percent(rwi.wins, rwi.total)).append("\n");
			apd.append("存活率：" + Utils.percent(rwi.alive, rwi.total)).append("\n");
			if(f!=Fraction.Wolf)
				apd.append("准确率：" +Utils.percent(rwi.saccuracy * 2 + rwi.vaccuracy, rwi.saccuracydemon * 2 + rwi.vaccuracydemon));
			return apd.toString();
		}
		return " 角色/阵营不存在";
	}

	@Override
	public void plus(WerewolfPlayerData another) {
		wins += another.wins;
		loses += another.loses;
		winaswolf += another.winaswolf;
		loseaswolf += another.loseaswolf;
		winasvill += another.winasvill;
		loseasvill += another.loseasvill;
		winasgod += another.winasgod;
		loseasgod += another.loseasgod;
		alive += another.alive;
		dieasvill += another.dieasvill;
		aliveasvill += another.aliveasvill;
		dieasgod += another.dieasgod;
		aliveasgod += another.aliveasgod;
		dieaswolf += another.dieaswolf;
		aliveaswolf += another.aliveaswolf;
		death += another.death;
		total = Math.max(another.total, total);
		for(Entry<Role, RoleWinInfo> rwi:another.winsrole.entrySet()) {
			this.winsrole.compute(rwi.getKey(),(k,v)->v==null?rwi.getValue():v.add(rwi.getValue()));
		}
	}

}
