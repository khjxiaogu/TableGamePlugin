package com.khjxiaogu.TableGames.werewolf;

import net.mamoe.mirai.contact.Member;

public class Elder extends Villager {
	boolean lifeUsed=false;
	public Elder(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}
	@Override
	public void onGameStart() {
		super.onGameStart();
		super.sendPrivate("你有两条命，可以被狼人杀两次，但是被毒或者驱逐也会死。");
	}
	@Override
	public Fraction getFraction() {
		return Fraction.Innocent;
	}
	@Override
	public String getRole() {
		return "长老";
	}

}
