package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;

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
