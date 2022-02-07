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
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class JiCR extends Role {
	static class Pistol extends Skill {
		public Pistol(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "手枪";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，可自杀";
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

	static class KillerCharc extends Skill {
		public KillerCharc(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(KillEvent.class, event -> {
				if (getRemain() > 0) {
					reduceRemain();
					event.andThen((gamex, ev) -> {
						((SkillEvent) ev).getSource().getAllSkills().forEach(e -> {
							if (e.getType() == SkillType.ATTACK) {
								e.addLocked(-1);
							}
						});
					});
				}
			});
		}

		@Override
		public String getName() {
			return "杀手本性";
		}

		@Override
		public String getDesc() {
			return "当本角色被杀，封锁杀人者所有杀人技，一次";
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
			return 1;
		}
	}

	public JiCR(GameManager room) {
		super(room);
		new Pistol(room, this);
		new KillerCharc(room, this);
	}

	@Override
	public String getName() {
		return "季愁然";
	}

	@Override
	public boolean isMale() {
		return false;
	}
}
