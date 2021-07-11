package com.khjxiaogu.TableGames.game.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class RoleDiedException extends InvalidSkillTargetException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RoleDiedException(String r) {
		super(r);
	}

	public RoleDiedException(Role role) {
		super(role);
	}

}
