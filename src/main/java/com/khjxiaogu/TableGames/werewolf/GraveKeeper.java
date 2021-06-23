package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;



public class GraveKeeper extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public GraveKeeper(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}



	@Override
	public String getJobDescription() {
		return "你属于神阵营，你在第二天晚上开始可以查看前一天白天投票死者是否狼人。";
	}

	@Override
	public void onTurn() {
		if (game.lastVoteOut != null) {
			game.logger.logSkill(this, game.lastVoteOut, "守墓人查验");
			if (game.lastVoteOut.getFraction() == Fraction.Wolf) {
				super.sendPrivate("上一个驱逐的是狼人");
			} else {
				super.sendPrivate("上一个驱逐的是好人");
			}
		} else {
			super.sendPrivate("前一天没有驱逐人");
		}
	}

	@Override
	public String getRole() {
		return "守墓人";
	}

	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}

	@Override
	public int getTurn() {
		return 2;
	}

}
