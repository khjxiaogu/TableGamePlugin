package com.khjxiaogu.TableGames.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.spwarframe.events.ExposeEvent;
import com.khjxiaogu.TableGames.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

public class TongL extends Role {

	static class Secretry extends Skill {

		public Secretry(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(DiedEvent.class, ev -> super.fireEvent(new ExposeEvent(ev.getKillBy().get(0).getSource())));
		}

		@Override
		public String getName() {
			return "秘书";
		}

		@Override
		public String getDesc() {
			return "被杀后公开杀人者身份，角色与阵营";
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

	static class Acid extends Skill {
		public Acid(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		public String getName() {
			return "硫酸";
		}

		@Override
		public String getDesc() {
			return "杀死一个女性角色，可自杀";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (target.isMale())
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

	public TongL(GameManager room) {
		super(room);
	}

	@Override
	public String getName() {
		return "童玲";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
