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
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.DeadAnnounceEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RebornEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.role.YaoFY.MapleMagic;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class GuoHY extends Role {
	static class CannotMapleMagic extends Skill {

		public CannotMapleMagic(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(SavedEvent.class, event -> {
				if (event.getSkill() instanceof MapleMagic) {
					event.reject();
				}
			});
		}

		@Override
		public String getName() {
			return "免疫枫之魔法";
		}

		@Override
		public String getDesc() {
			return "无法被枫之魔法救活";
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

	static class FakeDie extends Skill {

		public FakeDie(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "心脏骤停";
		}

		@Override
		public String getDesc() {
			return "可假死一个回合，不会有复活的消息";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return super.fireEvent(new DeadAnnounceEvent(owner));
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

	static class GoDie extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public GoDie(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "赴死";
		}

		@Override
		public String getDesc() {
			return "用自身生命拯救一个死者，无视回合，同时，暴露自己的角色与阵营，无法被救人技和特殊技复活";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				return false;
			Role target = game.getSkillPlayerByRole(params.get(0));
			if (target.isAlive()) {
				owner.checkCanSave(target);
			}
			// target.ensureAlive();
			return super.fireEvent(new RebornEvent(owner, target, this).setMainEvent(true).andThen((gm, ev) -> {
				game.fireEvent(new KillEvent(owner, owner, this));
				game.fireEvent(new KillEvent(owner, owner, this));
				owner.listen(SavedEvent.class, event -> event.reject());
			}));
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

	public GuoHY(GameManager room) {
		super(room);
		new CannotMapleMagic(room, this);
		new FakeDie(room, this);
		new GoDie(room, this);
	}

	@Override
	public String getName() {
		return "郭恒逸";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
