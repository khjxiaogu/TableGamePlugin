/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
