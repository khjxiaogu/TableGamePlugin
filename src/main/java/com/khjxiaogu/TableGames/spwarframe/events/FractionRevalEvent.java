package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;

public class FractionRevalEvent extends RevalEvent {

	@Override
	protected void doExecute(GameManager room) {
		getSource().sendMessage(getTarget().getPlayer()+"的阵营是"+getTarget().getRevalFraction());
		super.doExecute(room);
	}

	public FractionRevalEvent(Role source, Role target, Skill skill) { super(source, target, skill); }

}
