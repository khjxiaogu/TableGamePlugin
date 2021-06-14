package com.khjxiaogu.TableGames.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.spwarframe.role.Role;

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
