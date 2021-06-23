package com.khjxiaogu.TableGames.werewolf;


import com.khjxiaogu.TableGames.platform.AbstractPlayer;



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
		return 1.25;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.25;
	}

	public HardWolf(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}



}
