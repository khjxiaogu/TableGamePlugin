package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.CantSelfException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class ZhuLY extends Role {
	static class Backup extends Skill {
		public Backup(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "后援团";
		}

		@Override
		public String getDesc() {
			return "下个回合内阻止自己的角色被他人得知";
		}

		@Override
		protected boolean onSkillUse(List<String> params) {
			return super.fireEvent(
					new SkillEvent(owner, owner, this).setMainEvent(true).setPriority(11).andThen((gm, ev) -> {
						gm.HookEvents(owner, RoleRevalEvent.class, evt -> evt.reject());
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

	static class KillingGroup extends Skill {

		public KillingGroup(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "杀手团";
		}

		@Override
		public String getDesc() {
			return "杀死一个未暴露阵营的角色，不可自杀";
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			if (target == owner)
				throw new CantSelfException(target);
			if (target.fexposed)
				throw new InvalidSkillTargetException(target);

			target.ensureAlive();
			return super.fireEvent(new KillEvent(owner, target, this));
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

	public ZhuLY(GameManager room) {
		super(room);
		new Backup(room, this);
		new KillingGroup(room, this);
	}

	@Override
	public String getName() {
		return "朱莉雅";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
