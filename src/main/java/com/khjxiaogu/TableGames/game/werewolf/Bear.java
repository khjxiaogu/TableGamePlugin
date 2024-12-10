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

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class Bear extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onSkilledAccuracy() {
		return 0.1;
	}

	@Override
	public double onVotedAccuracy() {
		return 0.25;
	}

	public Bear(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public void onTurn() {
		Villager pt = this;
		while (true) {
			pt = pt.prev;
			if (pt.isDead()) {
				continue;
			}
			if (pt.getPredictorFraction() == Fraction.Wolf) {
				game.isBearRoared|=true;
				return;
			}
			break;
		}
		pt = this;
		while (true) {
			pt = pt.next;
			if (pt.isDead()) {
				continue;
			}
			if (pt.getPredictorFraction() == Fraction.Wolf) {
				game.isBearRoared|=true;
				return;
			}
			break;
		}

	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，如果你左右两边有狼人阵营角色，那么在猎人开枪前宣布“昨晚熊咆哮了。”，且不会检查当晚死亡了的人，包括被刀或者被毒等，但是会检查被猎人打死的人。如果熊左右没有狼人或者熊死亡，则固定显示“昨晚熊没有咆哮。”\r\n"
				+ "如果熊左右的人死了，则再会向后一个查，如果超过了人数列表的头或者尾，则从另一端开始继续。";
	}

	@Override
	public String getRole() {
		return "熊";
	}

	@Override
	public int getTurn() {
		return 4;
	}

}
