package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.BindEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class RanH extends Role {
	static class Sacrify extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Sacrify(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "献祭";
		}

		@Override
		public String getDesc() {
			return "以自己的生命为代价杀死一个人，不可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (target == owner)
				throw new CantSelfException(owner);
			return super.fireEvent(
					new KillEvent(owner, owner, this).setMainEvent(true).setInterruptable(false).andThen((g, ev) -> {
						super.fireEvent(new DiedEvent(owner, new KillEvent(owner, target, this)));
					}));
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

	static class Assign extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码", "游戏号码" };
		}

		public Assign(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "指派";
		}

		@Override
		public String getDesc() {
			return "强制捆绑两个角色，可对自己使用。";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 2)
				return false;
			Role target1 = game.getSkillPlayerByName(params.get(0));
			target1.ensureAlive();
			Role target2 = game.getSkillPlayerByName(params.get(1));
			target2.ensureAlive();
			return super.fireEvent(new BindEvent(owner, target1, target2, this).setMainEvent(true));
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

	public RanH(GameManager room) {
		super(room);
		new Sacrify(room, this);
		new Assign(room, this);
	}

	@Override
	public String getName() {
		return "苒浩";
	}

	@Override
	public boolean isMale() {
		return true;
	}

}
