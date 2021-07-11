package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.List;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.BindEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public class JiangTX extends Role {

	static class SnowMirage extends Skill {
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "角色名称" };
		}

		public SnowMirage(GameManager game, Role owner) {
			super(game, owner);
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByRole(params.get(0));
			owner.checkCanSave(target);
			return super.fireEvent(new SavedEvent(owner, target, this).setMainEvent(true).andThen((game, evt) -> {
				for (KillEvent crn : ((SavedEvent) evt).getSaved().getKillBy()) {
					game.fireEventLater(new KillEvent(crn.getSource(), crn.getTarget(), crn.getSkill()));
				}
			}));
		}

		@Override
		public SkillType getType() {
			return SkillType.SAVE;
		}

		@Override
		public String getName() {
			return "雪之幻境";
		}

		@Override
		public String getDesc() {
			return "让一个在本回合内死亡的人再活一个回合";
		}

		@Override
		public int getMaxRemain() {
			return 2;
		}

	}

	static class SnowTomb extends Skill {

		public SnowTomb(GameManager game, Role owner) {
			super(game, owner);
			owner.listen(KillEvent.class, event -> {
				if (getRemain() > 0) {
					reduceRemain();
					event.reject();
					super.fireEvent(new BindEvent(owner, owner, event.getSource(), this));
				}
			});
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
		public String getName() {
			return "雪墓";
		}

		@Override
		public String getDesc() {
			return "免疫一次杀人技的伤害，并与凶手捆绑";
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	public JiangTX(GameManager room) {
		super(room);
		new SnowMirage(room,this);
		new SnowTomb(room,this);
	}

	@Override
	public String getName() {
		return "江庭雪";
	}

	@Override
	public boolean isMale() {
		return false;
	}

}
