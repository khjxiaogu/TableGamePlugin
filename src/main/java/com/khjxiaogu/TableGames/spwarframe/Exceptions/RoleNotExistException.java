package com.khjxiaogu.TableGames.spwarframe.Exceptions;

public class RoleNotExistException extends InvalidSkillTargetException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public RoleNotExistException(String r) {
		super(r);
	}

}
