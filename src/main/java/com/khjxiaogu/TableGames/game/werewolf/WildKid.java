/**
 * Mirai Song Plugin
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
package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class WildKid extends Villager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4049480833298576003L;

	@Override
	public double onVotedAccuracy() {
		return 0.65;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.65;
	}

	@Override
	public String getJobDescription() {
		return "你属于第三方，你首夜可以选择一个榜样，如果榜样死亡，你将获得杀人技能，并且杀死好人或者狼人其中一方则胜利。";
	}

	@Override
	public int getTurn() {
		return 2;
	}

	public WildKid(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.Other;
	}

	@Override
	public String getRole() {
		return "野孩子";
	}
}
