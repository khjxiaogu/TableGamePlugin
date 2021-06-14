package com.khjxiaogu.TableGames.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.spwarframe.role.Role;

public class CantSaveException extends InvalidSkillTargetException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public CantSaveException(String r) {
		super(r);
	}

	public CantSaveException(Role save) {
		super(save);
	}

}
