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

public class YaoFX extends Role {
	static class Sensi extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码", "角色/阵营" };
		}

		public Sensi(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "敏感";
		}

		@Override
		public String getDesc() {
			return "可以得知一个玩家的阵营";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			return super.fireEvent(new FractionRevalEvent(owner, target, this));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

		@Override
		public boolean isUsableOn(GameTurn turn) {
			return turn == GameTurn.DAY || super.isUsableOn(turn);
		}

		@Override
		public boolean canOnceOnly() {
			return false;
		}
	}

	static class Pen extends Skill {
		public Pen(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "画笔";
		}

		@Override
		public String getDesc() {
			return "得知杀死某个阵营成员的所有凶手的身份和角色";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			return super.fireEvent(new SkillEvent(owner, target, this).andThen((gm, sk) -> {
				StringBuilder sb = new StringBuilder("凶手是：");
				target.lastkill.forEach(ev -> {
					sb.append("\n").append(ev.getSource().getPlayer());
					super.fireEvent(new RoleRevalEvent(owner, ev.getSource(), this));
				});
			}));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public int getMaxRemain() {
			return 3;
		}

		@Override
		public boolean isUsableOn(GameTurn turn) {
			return turn == GameTurn.DAY || super.isUsableOn(turn);
		}

		@Override
		public boolean canOnceOnly() {
			return false;
		}
	}

	static class Sad extends Skill {
		public Sad(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(SkillEvent.class, ev -> ev.setInterruptable(false));
		}

		@Override
		public String getName() {
			return "抑郁症";
		}

		@Override
		public String getDesc() {
			return "当该角色被杀或被使用特殊技时，干扰技无法救援";
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

	public YaoFX(GameManager room) {
		super(room);
		new Sensi(room, this);
		new Sad(room, this);
	}

	@Override
	public String getName() {
		return "姚枫茜";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
