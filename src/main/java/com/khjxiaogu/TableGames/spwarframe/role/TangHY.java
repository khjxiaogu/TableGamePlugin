package com.khjxiaogu.TableGames.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.events.BindEvent;
import com.khjxiaogu.TableGames.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

public class TangHY extends Role {
	static class Scissor extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		public Scissor(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		public String getName() {
			return "剪刀";
		}

		@Override
		public String getDesc() {
			return "杀死一个男性角色";
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			if (!target.isMale())
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

	static class IcyCold extends Skill {

		public IcyCold(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(BindEvent.class, ev -> ev.reject());
		}

		@Override
		public String getName() {
			return "冷若冰霜";
		}

		@Override
		public String getDesc() {
			return "无法被捆绑";
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

	static class SeaCry extends Skill {

		public SeaCry(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(DiedEvent.class,
					ev -> ev.getKillBy().get(0).getSource().getAllSkills().forEach(sk -> sk.addLocked(2)));
		}

		@Override
		public String getName() {
			return "海之泪滴";
		}

		@Override
		public String getDesc() {
			return "当本角色被杀，杀人者两个回合内无法使用任何技能";
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

	public TangHY(GameManager room) {
		super(room);
		new Scissor(room, this);
		new IcyCold(room, this);
		new SeaCry(room, this);
	}

	@Override
	public String getName() {
		return "唐海音";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
