package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;

public class RoleRevalEvent extends RevalEvent {

	public RoleRevalEvent(Role source, Role target, Skill skill) { super(source, target, skill); }
	@Override
	protected void doExecute(GameManager room) {
		getSource().sendMessage(getTarget().getPlayer()+"的角色是"+getTarget().getName());
		super.doExecute(room);
	}
}
