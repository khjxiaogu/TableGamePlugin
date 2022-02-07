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

import com.khjxiaogu.TableGames.data.GenericPlayerData;
import com.khjxiaogu.TableGames.utils.Utils;

public class WerewolfPlayerData implements GenericPlayerData<WerewolfPlayerData> {
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

	public WerewolfPlayerData() {
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

	public void win(Fraction frac, boolean isAlive) {
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

	public void lose(Fraction frac) {
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
			win(frac, isAlive);
			saccuracydemon += player.skilled;
			vaccuracydemon += player.voted;
			saccuracy += player.skillAccuracy + 0.25 * player.skilled;
			vaccuracy += player.voteAccuracy + 0.25 * player.voted;
			return true;
		} else if (win == Fraction.Wolf && frac == Fraction.Wolf) {
			win(frac, isAlive);
			return true;
		} else {
			if (frac != Fraction.Wolf) {
				saccuracydemon += player.skilled;
				vaccuracydemon += player.voted;
				saccuracy += player.skillAccuracy - (player.skilled > 0 ? 0.1 : 0);
				vaccuracy += player.voteAccuracy - (player.voted > 0 ? 0.1 : 0);
			}
			lose(frac);
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
		apd.append("综合准确率：").append(Utils.percent(saccuracy * 2 + vaccuracy, saccuracydemon * 2 + vaccuracydemon));
		return apd.toString();
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
	}
}
