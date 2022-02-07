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

public class HardWolf extends Werewolf {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean shouldSurvive() {
		return true;
	}

	@Override
	public double onVotedAccuracy() {
		return 1.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.5;
	}

	public HardWolf(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

}
