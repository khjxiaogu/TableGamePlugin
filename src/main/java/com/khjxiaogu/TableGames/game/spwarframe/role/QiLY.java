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
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.FractionRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class QiLY extends Role {
	static class FogRemoved extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public FogRemoved(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "雾散";
		}

		@Override
		public String getDesc() {
			return "窥探得知一个人的角色与阵营";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			return super.fireEvent(new RoleRevalEvent(owner, target, this))
					| super.fireEvent(new FractionRevalEvent(owner, target, this));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public boolean isUsableOn(GameTurn turn) {
			return turn == GameTurn.DAY || super.isUsableOn(turn);
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

		@Override
		public boolean canOnceOnly() {
			return false;
		}
	}

	static class SnowFlower extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public SnowFlower(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "雪莲";
		}

		@Override
		public String getDesc() {
			return "恢复一个角色所有的技能次数，无法解除封锁，可以对自己使用";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			return super.fireEvent(new SkillEvent(owner, target, this).setMainEvent(true).andThen((game, ev) -> {
				((SkillEvent) ev).getTarget().getAllSkills().forEach(sk -> sk.resetRemain());
			}));
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

	public QiLY(GameManager room) {
		super(room);
		new FogRemoved(room, this);
		new SnowFlower(room, this);
	}

	@Override
	public String getName() {
		return "祁连瑶";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
