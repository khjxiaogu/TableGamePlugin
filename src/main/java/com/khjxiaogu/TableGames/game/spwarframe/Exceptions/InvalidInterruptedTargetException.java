package com.khjxiaogu.TableGames.game.spwarframe.Exceptions;

public class InvalidInterruptedTargetException extends SkillException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private String target;

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public InvalidInterruptedTargetException(String num) {
		target = num;
	}

}
