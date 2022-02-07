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
package com.khjxiaogu.TableGames.game.undercover;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class UnderCoverPreserve extends PreserveInfo<UnderCoverGame> {

	public UnderCoverPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 7;
	}

	@Override
	protected int getMinMembers() {
		return 5;
	}

	@Override
	protected int getMaxMembers() {
		return 9;
	}
	static GameCreater<UnderCoverGame> gc=new DefaultGameCreater<>(UnderCoverGame.class);
	@Override
	protected GameCreater<UnderCoverGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "谁是卧底";
	}

}
