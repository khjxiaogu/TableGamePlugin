package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;

public class Coroner extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public Coroner(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}

	@Override
	public void onTurn() {
		StringBuilder sb = new StringBuilder("昨晚死者名单：");
		if (!game.tokill.isEmpty()) {
			for (Villager me : game.tokill) {
				game.logger.logSkill(this, me, "验尸官查验");
				sb.append("\n").append(me.getMemberString()).append(" 是")
				.append(me.getFraction() == Fraction.Wolf ? "狼人" : "好人").append("，死于")
				.append(me.getEffectiveDiedReason().desc);
			}
		} else {
			sb.append("昨晚无死者");
		}
		sendPrivate(sb.toString());
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你每天白天前可以知道每晚所有死者的最终死因和阵营。";
	}

	@Override
	public String getRole() {
		return "验尸官";
	}

	@Override
	public int getTurn() {
		return 4;
	}
}
