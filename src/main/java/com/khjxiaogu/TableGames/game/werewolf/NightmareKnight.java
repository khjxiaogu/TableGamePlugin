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

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;

public class NightmareKnight extends Werewolf {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 1.5;
	}

	@Override
	public double onSkilledAccuracy() {
		return 1;
	}

	@Override
	public String getJobDescription() {
		return "你属于狼阵营，你可以与其他狼人一起杀人，但是你不会在晚上死亡。并且在第一次被使用技能时会反伤杀死发动技能者。";
	}

	public NightmareKnight(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	boolean isSkillUsed = false;

	@Override
	public boolean shouldSurvive(DiedReason dir) {
		return true;
	}

	@Override
	public void doDaySkillPending(String s) {
	}

	@Override
	public void onTurn() {
	}

	@Override
	public void addDaySkillListener() {
	}

	@Override
	public String getRole() {
		return "恶灵骑士";
	}
}
