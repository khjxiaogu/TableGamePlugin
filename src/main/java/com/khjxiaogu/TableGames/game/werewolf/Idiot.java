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

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;

public class Idiot extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onSkilledAccuracy() {
		return 0.4;
	}

	public Idiot(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你被投票驱逐后不会死，每天依然能发言。";
	}

	boolean canVote = true;

	@Override
	public void vote() {
		if (canVote) {
			super.vote();
		}
	}

	@Override
	public void onDied(DiedReason dir, boolean shouldSkill) {
		if (dir != DiedReason.Vote) {
			super.onDied(dir, shouldSkill);
		} else {
			super.diedReasonStack.remove(DiedReason.Vote);
			game.logger.logRaw(this.getMemberString(this) + " 白痴出局");
			isDead = false;
			canVote = false;
			sendForName("被驱逐了，身份是白痴。");
		}
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public String getRole() {
		return "白痴";
	}

	@Override
	public boolean shouldSurvive(DiedReason dir) {
		return dir==DiedReason.Vote;
	}
}
