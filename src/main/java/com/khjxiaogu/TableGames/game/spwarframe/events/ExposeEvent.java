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
package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class ExposeEvent extends Event {
	Role target;
	public ExposeEvent(Role target) {
		this.target=target;
	}

	public Role getTarget() {
		return target;
	}

	public void setTarget(Role target) {
		this.target = target;
	}

	@Override
	protected void doExecute(GameManager room) {
		target.expose();
		room.sendAll(target.getPlayer()+"的阵营是"+target.getFraction().getName()+"，身份是"+target.getName());
		super.doExecute(room);
	}
	@Override
	public String toString() {
		return target.getName()+" 暴露了身份！";
	}
}
