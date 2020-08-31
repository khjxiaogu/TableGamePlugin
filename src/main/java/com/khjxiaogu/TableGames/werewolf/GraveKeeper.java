package com.khjxiaogu.TableGames.werewolf;

import net.mamoe.mirai.contact.Member;

public class GraveKeeper extends Villager{

	public GraveKeeper(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onTurn() {
		if(game.lastVoteOut!=null) {
			if(game.lastVoteOut.getFraction()==Fraction.Wolf)
				super.sendPrivate("上一个驱逐的是狼人");
			else
				super.sendPrivate("上一个驱逐的是好人");
		}else
			super.sendPrivate("前一天没有驱逐人");
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
