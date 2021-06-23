package com.khjxiaogu.TableGames.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.events.FractionRevalEvent;
import com.khjxiaogu.TableGames.spwarframe.events.RebornEvent;
import com.khjxiaogu.TableGames.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

public class WangXH extends Role {
	static class Pathing extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public Pathing(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "指路";
		}

		@Override
		public String getDesc() {
			return "指引一个死者回到人世，无论回合，无法自救";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByRole(params.get(0));
			if (target == owner)
				throw new CantSelfException(target);
			if (target.isAlive()) {
				owner.checkCanSave(target);
			}
			return super.fireEvent(new RebornEvent(owner, target, this).setMainEvent(true));
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

	static class Map extends Skill {

		public Map(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码", "角色/阵营" };
		}

		@Override
		public String getName() {
			return "地图";
		}

		@Override
		public String getDesc() {
			return "探查一个玩家的角色或阵营";
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
				return false;
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

	public WangXH(GameManager room) {
		super(room);
		new Pathing(room, this);
		new Map(room, this);
	}

	@Override
	public String getName() {
		return "汪星涵";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
