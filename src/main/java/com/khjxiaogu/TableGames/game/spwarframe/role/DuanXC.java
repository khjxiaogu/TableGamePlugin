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
import com.khjxiaogu.TableGames.game.spwarframe.events.FractionRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class DuanXC extends Role {
	static class Return extends Skill {

		public Return(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "本境再临";
		}

		@Override
		public String getDesc() {
			return "保护一个回合内所有人的生命，特殊技照常损耗";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return super.fireEvent(
					new SkillEvent(owner, null, this).setMainEvent(true).setPriority(10).andThen((game, role) -> {
						game.HookEvents(KillEvent.class, event -> event.cancel());
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

	static class DoubleChar extends Skill {
		public DoubleChar(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(RoleRevalEvent.class, event -> event.reject());
			owner.listen(FractionRevalEvent.class, event -> game
					.fireEvent(new FractionRevalEvent(owner, event.getSource(), this).setInterruptable(false)));
		}

		@Override
		public String getName() {
			return "双向人格";
		}

		@Override
		public String getDesc() {
			return "无法被探查到角色，被探查阵营则同时反向探查探查者阵营";
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

	public DuanXC(GameManager room) {
		super(room);
		new Return(room, this);
		new DoubleChar(room, this);
	}

	@Override
	public String getName() {
		return "端贤冲";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
