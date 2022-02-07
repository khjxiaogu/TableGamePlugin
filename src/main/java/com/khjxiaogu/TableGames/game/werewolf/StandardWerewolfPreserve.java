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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class StandardWerewolfPreserve extends PreserveInfo<WerewolfGame> {

	public StandardWerewolfPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 12;
	}

	@Override
	protected int getMinMembers() {
		return 12;
	}

	@Override
	protected int getMaxMembers() {
		return 12;
	}

	static GameCreater<WerewolfGame> gc = new StandardWerewolfCreater();

	@Override
	protected GameCreater<WerewolfGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "标准狼人杀";
	}

}
