package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class KillEvent extends SkillEvent {

	public KillEvent(Role source, Role target, Skill skill) { super(source, target, skill);priority=1; }

	@Override
	protected void doExecute(GameManager room) {
		if(getSource()!=getTarget()) {
			getSource().addKillCount();
		}
		super.doExecute(room);
	}

}
