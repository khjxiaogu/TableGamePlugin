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
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class InterrputedEvent extends SkillEvent {
	private Skill targetSkill;

	public InterrputedEvent(Role source,Skill target, Skill skill) {
		super(source,target.getOwner(), skill);
		targetSkill = target;
		priority=10;
	}

	@Override
	protected void doExecute(GameManager room) {
		super.doExecute(room);
		getSource().room.CheckEvents(SkillEvent.class,event->{
			if(event.getSkill().equals(targetSkill)) {
				event.cancel();
			}
		});
	}
}
