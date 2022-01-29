package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class WildKid extends Villager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4049480833298576003L;
	@Override
	public double onVotedAccuracy() {
		return 0.65;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.65;
	}
	@Override
	public String getJobDescription() {
		return "你属于第三方，你首夜可以选择一个榜样，如果榜样死亡，你将获得杀人技能，并且杀死好人或者狼人其中一方则胜利。";
	}
	@Override
	public int getTurn() {
		return 2;
	}
	public WildKid(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}
	@Override
	public Fraction getRealFraction() {
		return Fraction.Other;
	}

	@Override
	public String getRole() {
		return "野孩子";
	}
}
