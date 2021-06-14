package com.khjxiaogu.TableGames.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.InvalidSkillTargetException;
import com.khjxiaogu.TableGames.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.spwarframe.events.RoleRevalEvent;
import com.khjxiaogu.TableGames.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

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
