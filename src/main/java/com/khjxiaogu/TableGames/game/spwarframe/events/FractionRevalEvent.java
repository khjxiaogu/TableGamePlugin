package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class FractionRevalEvent extends RevalEvent {

	@Override
	protected void doExecute(GameManager room) {
		getSource().sendMessage(getTarget().getPlayer()+"的阵营是"+getTarget().getRevalFraction());
		super.doExecute(room);
	}

	public FractionRevalEvent(Role source, Role target, Skill skill) { super(source, target, skill); }

}
