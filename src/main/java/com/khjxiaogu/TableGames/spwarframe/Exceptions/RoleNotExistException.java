package com.khjxiaogu.TableGames.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.spwarframe.role.Role;

public class RoleNotExistException extends InvalidSkillTargetException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RoleNotExistException(String r) {
		super(r);
	}

}
