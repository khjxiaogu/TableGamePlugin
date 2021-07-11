package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SystemEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class RanXY extends Role {
	static class Killing extends Skill {
		public Killing(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "大力";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				return false;
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
			return 1;
		}

	}

	static class Sicking extends Skill {

		public Sicking(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "病态";
		}

		@Override
		public String getDesc() {
			return "在下个回合无法被任何技能影响";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return super.fireEvent(new SkillEvent(owner, owner, this).setMainEvent(true).andThen((gm, ev) -> {
				gm.fireEvent(new SystemEvent(GameTurn.ATTACK).andThen((gmx, evx) -> {
					gm.HookEvents(SkillEvent.class, eva -> {
						if (eva.getTarget() == owner) {
							eva.reject();
						}
					});
				}));
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

	static class Intern extends Skill {
		public Intern(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(SavedEvent.class, ev -> {
				if (ev.getSource().isMale()) {
					ev.reject();
				}
			});
		}

		@Override
		public String getName() {
			return "内向";
		}

		@Override
		public String getDesc() {
			return "男性角色的救人技对你无效";
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

	public RanXY(GameManager room) {
		super(room);
		new Killing(room, this);
		new Sicking(room, this);
		new Intern(room, this);
	}

	@Override
	public String getName() {
		return "苒雪忆";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
