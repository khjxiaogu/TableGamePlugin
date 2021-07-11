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
