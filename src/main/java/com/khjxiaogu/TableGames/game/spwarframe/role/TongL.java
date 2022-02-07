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
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.ExposeEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class TongL extends Role {

	static class Secretry extends Skill {

		public Secretry(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(DiedEvent.class, ev -> super.fireEvent(new ExposeEvent(ev.getKillBy().get(0).getSource())));
		}

		@Override
		public String getName() {
			return "秘书";
		}

		@Override
		public String getDesc() {
			return "被杀后公开杀人者身份，角色与阵营";
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

	static class Acid extends Skill {
		public Acid(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "硫酸";
		}

		@Override
		public String getDesc() {
			return "杀死一个女性角色，可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (target.isMale())
				throw new InvalidSkillTargetException(target);
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

	public TongL(GameManager room) {
		super(room);
	}

	@Override
	public String getName() {
		return "童玲";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
