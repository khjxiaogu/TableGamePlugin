package com.khjxiaogu.TableGames.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

public class YangYQ extends Role {
	static class Kill extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Kill(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "斩杀";
		}

		@Override
		public String getDesc() {
			return "杀死一个杀过人的人，不可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (target == owner)
				throw new CantSelfException(target);
			if (target.getKillCount() == 0)
				throw new InvalidSkillTargetException(target);
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

	static class RainCry extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public RainCry(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "雨泪凝结";
		}

		@Override
		public String getDesc() {
			return "拯救一个没杀过人的玩家的生命，可自救";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			if (target.getKillCount() > 0)
				throw new InvalidSkillTargetException(target);
			return super.fireEvent(new SavedEvent(owner, target, this).setMainEvent(true));
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

	public YangYQ(GameManager room) {
		super(room);
		new Kill(room, this);
		new RainCry(room, this);
	}

	@Override
	public String getName() {
		return "扬雨晴";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
