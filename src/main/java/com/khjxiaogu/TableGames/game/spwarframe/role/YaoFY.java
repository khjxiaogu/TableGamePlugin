/**
 * Mirai Song Plugin
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

public class YaoFY extends Role {
	static class MapleMagic extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public MapleMagic(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "枫之魔法";
		}

		@Override
		public String getDesc() {
			return "拯救一个本回合内的死者的生命，可自救。使用后，枫之恶魔被封锁";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {

			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			owner.getSkill(MapleDemon.class).addLocked(-1);
			return super.fireEvent(new SavedEvent(owner, target, this));
		}

		@Override
		public SkillType getType() {
			return SkillType.SAVE;
		}

		@Override
		public int getMaxRemain() {
			return 2;
		}

	}

	static class MapleDemon extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public MapleDemon(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "枫之恶魔";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，不可自杀。使用后，枫之魔法被封锁。";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			if (target == owner)
				throw new CantSelfException(target);
			owner.getSkill(MapleMagic.class).addLocked(-1);
			target.ensureAlive();
			return super.fireEvent(new KillEvent(owner, target, this));
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

	public YaoFY(GameManager room) {
		super(room);
		new MapleMagic(room, this);
		new MapleDemon(room, this);
	}

	@Override
	public String getName() {
		return "姚枫怡";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
