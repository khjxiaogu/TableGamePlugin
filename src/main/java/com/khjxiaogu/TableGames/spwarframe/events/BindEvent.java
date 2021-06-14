package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;

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
