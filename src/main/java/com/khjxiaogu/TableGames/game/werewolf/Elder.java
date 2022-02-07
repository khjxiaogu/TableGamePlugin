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

public class Elder extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 0.35;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.3;
	}

	@Override
	public String getJobDescription() {
		return "你属于民阵营，你有两条命，可以被狼人杀两次，但是被毒或者驱逐也会死。";
	}

	public Elder(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	public boolean lifeUsed = false;

	@Override
	public Fraction getRealFraction() {
		return Fraction.Innocent;
	}

	@Override
	public String getRole() {
		return "长老";
	}

}
