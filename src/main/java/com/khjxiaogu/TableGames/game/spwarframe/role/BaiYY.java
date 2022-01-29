package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.FractionRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class BaiYY extends Role {
	static class RubicCube extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码", "角色/阵营" };
		}

		public RubicCube(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "魔方占卜";
		}

		@Override
		public String getDesc() {
			return "可以得知一个玩家的角色或阵营";
		}

		@Override
		public boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 2)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			if (params.get(1).equals("角色"))
				return super.fireEvent(new RoleRevalEvent(owner, target, this).setMainEvent(true));
			else if (params.get(1).equals("阵营"))
				return super.fireEvent(new FractionRevalEvent(owner, target, this).setMainEvent(true));
			else
				throw new InvalidSkillParamException(this);
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
		public boolean canOnceOnly() {
			return false;
		}

		@Override
		public boolean isUsableOn(GameTurn turn) {
			return turn == GameTurn.DAY || super.isUsableOn(turn);
		}

	}

	static class GhostGirl extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public GhostGirl(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "幽灵少女";
		}

		@Override
		public String getDesc() {
			return "杀死一个人，可自杀，本技能使用后魔方占卜被封锁";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			owner.getSkill(RubicCube.class).addLocked(-1);
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

	public BaiYY(GameManager room) {
		super(room);
		new RubicCube(room, this);
		new GhostGirl(room, this);
	}

	@Override
	public String getName() {
		return "白悠悠";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
