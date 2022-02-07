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
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.BindEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class TangHY extends Role {
	static class Scissor extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Scissor(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "剪刀";
		}

		@Override
		public String getDesc() {
			return "杀死一个男性角色";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (!target.isMale())
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

	static class IcyCold extends Skill {

		public IcyCold(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(BindEvent.class, ev -> ev.reject());
		}

		@Override
		public String getName() {
			return "冷若冰霜";
		}

		@Override
		public String getDesc() {
			return "无法被捆绑";
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

	static class SeaCry extends Skill {

		public SeaCry(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(DiedEvent.class,
					ev -> ev.getKillBy().get(0).getSource().getAllSkills().forEach(sk -> sk.addLocked(2)));
		}

		@Override
		public String getName() {
			return "海之泪滴";
		}

		@Override
		public String getDesc() {
			return "当本角色被杀，杀人者两个回合内无法使用任何技能";
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

	public TangHY(GameManager room) {
		super(room);
		new Scissor(room, this);
		new IcyCold(room, this);
		new SeaCry(room, this);
	}

	@Override
	public String getName() {
		return "唐海音";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
