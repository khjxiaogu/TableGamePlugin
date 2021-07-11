package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;



public class Idiot extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onSkilledAccuracy() {
		return 0.4;
	}

	public Idiot(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}



	@Override
	public String getJobDescription() {
		return "你属于神阵营，你被投票驱逐后不会死，每天依然能发言。";
	}

	boolean canVote = true;

	@Override
	public void vote() {
		if (canVote) {
			super.vote();
		}
	}

	@Override
	public void onDied(DiedReason dir, boolean shouldSkill) {
		if (dir != DiedReason.Vote) {
			super.onDied(dir, shouldSkill);
		} else {
			game.logger.logRaw(getNameCard() + " 白痴出局");
			isDead = false;
			canVote = false;
			sendPublic("被驱逐了，身份是白痴。");
		}
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public String getRole() {
		return "白痴";
	}
}
