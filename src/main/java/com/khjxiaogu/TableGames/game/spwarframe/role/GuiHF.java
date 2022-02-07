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
package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.InterrputedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class GuiHF extends Role {
	static class IntelEye extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "技能号码" };
		}

		public IntelEye(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "慧眼独具";
		}

		@Override
		public String getDesc() {
			return "干扰一次特殊技";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Skill target = owner.getInterruptSkillByNum(params.get(0));
			target.ensureInterruptType(SkillType.SPECIAL);
			return super.fireEvent(new InterrputedEvent(owner, target, this).setMainEvent(true));
		}

		@Override
		public SkillType getType() {
			return SkillType.INTERRUPT;
		}

		@Override
		public int getMaxRemain() {
			return 2;
		}

	}

	static class DarkNess extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public DarkNess(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "黑化";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，不可自杀，使用本技能后慧眼独具被封锁";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (owner == target)
				throw new CantSelfException(target);
			owner.getSkill(IntelEye.class).addLocked(-1);
			return super.fireEvent(new KillEvent(owner, target, this).setMainEvent(true));
		}

		@Override
		public SkillType getType() {
			return SkillType.ATTACK;
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	public GuiHF(GameManager room) {
		super(room);
		new IntelEye(room, this);
		new DarkNess(room, this);
	}

	@Override
	public String getName() {
		return "归海枫";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
