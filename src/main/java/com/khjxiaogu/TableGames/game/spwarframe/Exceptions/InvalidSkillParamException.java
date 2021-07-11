package com.khjxiaogu.TableGames.game.spwarframe.Exceptions;

import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;

public class InvalidSkillParamException extends SkillException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	String[] shouldbe;
	Skill skill;

	public String getShouldbe() {
		StringBuilder sb = new StringBuilder(skill.getName());
		for (String s : shouldbe) {
			sb.append(" <").append(s).append(">");
		}
		return sb.toString();
	}

	public void setShouldbe(String[] shouldbe) {
		this.shouldbe = shouldbe;
	}

	public InvalidSkillParamException(Skill skill, String[] shouldbe) {
		super();
		this.shouldbe = shouldbe;
		this.skill = skill;
	}

	public InvalidSkillParamException(Skill skill) {
		super();
		shouldbe = skill.getSpecialParamType();
		this.skill = skill;
	}
}
