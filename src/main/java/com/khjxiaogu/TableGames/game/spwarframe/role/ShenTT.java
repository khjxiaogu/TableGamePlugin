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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.BindEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class ShenTT extends Role {
	static class Loving extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Loving(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "痴情";
		}

		@Override
		public String getDesc() {
			return "可与一位女性角色捆绑";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			return super.fireEvent(new BindEvent(owner, owner, target, this));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	static class Cut extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Cut(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "奋力劈砍";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，冷却时间一回合，可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (super.fireEvent(new KillEvent(owner, target, this).setMainEvent(true))) {
				addLocked(1);
				return true;
			}
			return false;
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

	public ShenTT(GameManager room) {
		super(room);
		new Loving(room, this);
		new Cut(room, this);
	}

	@Override
	public String getName() {
		return "沈听涛";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
