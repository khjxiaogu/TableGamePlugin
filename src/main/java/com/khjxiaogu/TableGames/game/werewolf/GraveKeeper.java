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

public class GraveKeeper extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public GraveKeeper(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你在第二天晚上开始可以查看前一天白天投票死者是否狼人。";
	}

	@Override
	public void onTurn() {
		if (game.lastVoteOut != null) {
			game.logger.logSkill(this, game.lastVoteOut, "守墓人查验");
			if (game.lastVoteOut.getPredictorFraction() == Fraction.Wolf) {
				super.sendPrivate("上一个驱逐的是狼人");
			} else {
				super.sendPrivate("上一个驱逐的是好人");
			}
		} else {
			super.sendPrivate("前一天没有驱逐人");
		}
	}

	@Override
	public String getRole() {
		return "守墓人";
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public int getTurn() {
		return 2;
	}

}
