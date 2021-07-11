package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class RebornEvent extends SavedEvent {

	public RebornEvent(Role source, Role target, Skill skill) { super(source, target, skill); }

	@Override
	protected void doExecute(GameManager room) {
		super.doExecute(room);
	}

}
