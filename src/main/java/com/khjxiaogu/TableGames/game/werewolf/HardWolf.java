package com.khjxiaogu.TableGames.game.werewolf;


import com.khjxiaogu.TableGames.platform.AbstractUser;



public class HardWolf extends Werewolf {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean shouldSurvive() {
		return true;
	}

	@Override
	public double onVotedAccuracy() {
		return 1.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.5;
	}

	public HardWolf(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}



}
