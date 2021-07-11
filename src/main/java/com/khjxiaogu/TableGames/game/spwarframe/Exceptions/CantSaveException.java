package com.khjxiaogu.TableGames.game.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

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
