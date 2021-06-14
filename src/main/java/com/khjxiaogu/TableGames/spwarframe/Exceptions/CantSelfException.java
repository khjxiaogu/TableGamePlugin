package com.khjxiaogu.TableGames.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.spwarframe.role.Role;

public class CantSelfException extends InvalidSkillTargetException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CantSelfException(String r) {
		super(r);
	}

	public CantSelfException(Role target) {
		super(target);
	}

}
