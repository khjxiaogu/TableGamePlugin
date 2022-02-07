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

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class WerewolfPreserve extends PreserveInfo<WerewolfGame> {

	public WerewolfPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 9;
	}

	@Override
	protected int getMinMembers() {
		return 8;
	}

	@Override
	protected int getMaxMembers() {
		return 18;
	}

	static GameCreater<WerewolfGame> gc = new DefaultGameCreater<>(WerewolfGame.class);

	@Override
	protected GameCreater<WerewolfGame> getGameClass() {
		return gc;
	}

	@Override
	protected boolean isAvailableConfig(AbstractUser ar, String item, String set) {
		if (item.equals("vip")) {
			if (GlobalMain.credit.get(ar.getId()).hasItem("狼人杀vip券")) {
				if (set.equals("神") || set.equals("狼") || set.equals("民")) {
					return true;
				}
				ar.sendPrivate("阵营错误，必须为“神”、“狼”、“民”之一");
			}
			ar.sendPrivate("狼人杀vip券不足！");
		}
		return false;
	}

	@Override
	public String getName() {
		return "狼人杀";
	}

}
