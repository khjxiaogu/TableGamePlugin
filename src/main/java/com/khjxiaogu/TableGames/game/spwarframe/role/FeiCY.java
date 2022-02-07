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
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class FeiCY extends Role {
	static class Disguise extends Skill {

		public Disguise(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(RoleRevalEvent.class, event -> event.reject());
		}

		@Override
		public String getName() {
			return "伪装";
		}

		@Override
		public String getDesc() {
			return "无法被探查角色";
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

	static class Song extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Song(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "歌谣";
		}

		@Override
		public String getDesc() {
			return "公开一次已发生的杀人事件中，杀人者的身份，角色与阵营";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			if (target.lastkill == null)
				throw new InvalidSkillTargetException(target);
			return super.fireEvent(new SkillEvent(owner, target, this).setMainEvent(true).andThen((game, ev) -> {
				List<KillEvent> kevs = ((SkillEvent) ev).getTarget().lastkill;
				Role src = kevs.get(kevs.size() - 1).getSource();
				game.sendAll("上次杀死" + target.getPlayer() + "的人身份为" + src.getPlayer() + "，角色为" + src.getRevalPlayer()
				+ "，阵营为" + src.getRevalFraction() + "。");
			}));
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
		public boolean canOnceOnly() {
			return false;
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	public FeiCY(GameManager room) {
		super(room);
		new Disguise(room, this);
		new Song(room, this);
	}

	@Override
	public String getName() {
		return "翡翠月";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
