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

import java.util.LinkedList;
import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class DiedEvent extends Event {
	private List<KillEvent> killBy=new LinkedList<>();
	private Role target;
	public DiedEvent(Role target) { super();this.target=target; }
	public DiedEvent(Role target,KillEvent by) { super();this.target=target;killBy.add(by); }
	public DiedEvent(Role target, List<KillEvent> by) {
		this.target=target;
		killBy.addAll(by);
	}
	public Role getTarget() {
		return target;
	}
	public List<KillEvent> getKillBy() {
		return killBy;
	}
	public void populateKill(KillEvent ev) {
		killBy.add(ev);
	}
	@Override
	public String toString() {
		return target.getName()+" 被杀了。";
	}
}