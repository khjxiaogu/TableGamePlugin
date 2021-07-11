package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;



public class Elder extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 0.35;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.3;
	}

	@Override
	public String getJobDescription() {
		return "你属于民阵营，你有两条命，可以被狼人杀两次，但是被毒或者驱逐也会死。";
	}

	public Elder(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	public boolean lifeUsed = false;
	@Override
	public Fraction getRealFraction() {
		return Fraction.Innocent;
	}

	@Override
	public String getRole() {
		return "长老";
	}

}
