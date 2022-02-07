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
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.role.BaiYY.RubicCube;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class QinMeng extends Role {
	static class Cubeless extends Skill {

		public Cubeless(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(RoleRevalEvent.class, ev -> {
				if (ev.getSkill() instanceof RubicCube) {
					ev.reject();
				}
			});
		}

		@Override
		public String getName() {
			return "无法占卜";
		}

		@Override
		public String getDesc() {
			return "无法被白悠悠占卜到身份";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return false;
		}

		@Override
		public SkillType getType() {
			return SkillType.PASSIVE;
		}

		@Override
		public int getMaxRemain() {
			return 0;
		}

	}

	static class HikePickaxe extends Skill {

		boolean isOnceOnly = true;

		public HikePickaxe(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "登山镐";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，可自杀";
		}

		@Override
		public boolean onTurnEnd() {
			isOnceOnly = true;
			return super.onTurnEnd();
		}

		@Override
		public boolean canOnceOnly() {
			return isOnceOnly;
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			return super.fireEvent(new KillEvent(owner, target, this).setMainEvent(true));
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

	static class Strong extends Skill {
		public Strong(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "体力充沛";
		}

		@Override
		public String getDesc() {
			return "可以在下个回合内使用所有杀人技次数";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return super.fireEvent(new SkillEvent(owner, owner, this).setMainEvent(true).andThen((gm, ev) -> {
				((SkillEvent) ev).getTarget().getSkill(HikePickaxe.class).isOnceOnly = false;
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

	public QinMeng(GameManager room) {
		super(room);
		new Cubeless(room, this);
		new HikePickaxe(room, this);
	}

	@Override
	public String getName() {
		return "秦猛";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
