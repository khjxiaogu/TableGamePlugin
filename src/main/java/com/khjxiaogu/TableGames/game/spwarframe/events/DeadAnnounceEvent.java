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
package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class DeadAnnounceEvent extends Event {
	private int counter=0;
	private Role target;
	public DeadAnnounceEvent(Role target) { super();this.target=target; }
	public void addCounter() {
		counter++;
	}
	public void addCounter(int c) {
		counter+=c;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public Role getTarget() {
		return target;
	}
	public int getCounter() { return counter; }
	@Override
	public String toString() {
		return target.getName()+"死亡，被杀次数："+counter;
	}
}
