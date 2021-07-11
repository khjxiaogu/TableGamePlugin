package com.khjxiaogu.TableGames.game.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class InvalidSkillTargetException extends SkillException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	String role;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public InvalidSkillTargetException(String r) {
		role = r;
	}

	public InvalidSkillTargetException(Role target) {
		role=target.getName();
	}

}
