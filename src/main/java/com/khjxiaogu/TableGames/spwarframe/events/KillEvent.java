package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;

public class KillEvent extends SkillEvent {

	public KillEvent(Role source, Role target, Skill skill) { super(source, target, skill);priority=1; }

	@Override
	protected void doExecute(GameManager room) {
		if(this.getSource()!=this.getTarget()) {
			this.getSource().addKillCount();
		}
		super.doExecute(room);
	}
	
}
