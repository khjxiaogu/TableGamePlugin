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

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class BindEvent extends SkillEvent {
	@Override
	protected void doExecute(GameManager room) {
		super.getTarget().bind(target2);
		super.doExecute(room);
	}
	protected Role target2;
	public BindEvent(Role source,Role target1,Role target2, Skill skill) { super(source, target1, skill);this.target2=target2; }
	@Override
	public boolean isTargeting(Role role) {
		return role==target2||super.isTargeting(role);
	}
	@Override
	public String toString() {
		return getSource().getName()+" 使用了技能["+getSkill().getName()+"]把" + target2.getName() + "与" +getTarget().getName() + "绑定了";
	}

}
