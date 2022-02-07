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
package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class SkillEvent extends Event {
	private Role Source;
	private Role Target;
	private Skill skill;
	private boolean isMainEvent=true;
	public boolean isMainEvent() {
		return isMainEvent;
	}
	public SkillEvent setMainEvent(boolean isMainEvent) {
		this.isMainEvent = isMainEvent;
		return this;
	}
	private boolean Interruptable=true;
	public boolean isInterruptable() {
		return Interruptable;
	}
	public Event setInterruptable(boolean interruptable) {
		Interruptable = interruptable;
		return this;
	}
	public Skill getSkill() { return skill; }

	public SkillEvent(Role source, Role target, Skill skill) {
		super();
		Source = source;
		Target = target;
		this.skill = skill;
		priority=5;
	}
	public Role getSource() { return Source; }
	public Role getTarget() { return Target; }
	@Override
	public boolean isTargeting(Role role) {return Target==role;}
}
