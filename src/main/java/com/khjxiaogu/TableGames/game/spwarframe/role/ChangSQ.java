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
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class ChangSQ extends Role {
	static class Killing extends Skill {

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public Killing(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "为非作歹";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，不可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (target != owner)
				return super.fireEvent(new KillEvent(owner, target, this).setMainEvent(true));
			throw new CantSelfException(target);
		}

		@Override
		public SkillType getType() {
			return SkillType.ATTACK;
		}

		@Override
		public int getMaxRemain() {
			return 2;
		}

	}

	static class Healing extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public Healing(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "急救包扎";
		}

		@Override
		public String getDesc() {
			return "拯救一个本回合内的一个死者的生命，可自救";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByRole(params.get(0));
			owner.checkCanSave(target);
			return super.fireEvent(new SavedEvent(owner, target, this).setMainEvent(true));
		}

		@Override
		public SkillType getType() {
			return SkillType.SAVE;
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	public ChangSQ(GameManager room) {
		super(room);
		new Killing(room, this);
		new Healing(room, this);
	}

	@Override
	public String getName() {
		return "常舍青";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
