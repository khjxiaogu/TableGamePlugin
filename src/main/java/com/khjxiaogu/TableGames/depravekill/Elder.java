package com.khjxiaogu.TableGames.depravekill;

import net.mamoe.mirai.contact.Member;

public class Elder extends Villager {
	boolean lifeUsed=false;
	public Elder(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public void onGameStart() {
		super.onGameStart();
		super.sendPrivate("你有两条命，可以被魅魔干两次，但是被毒或者驱逐也会死。");
	}
	@Override
	public Fraction getFraction() {
		return Fraction.Innocent;
	}
	@Override
	public String getRole() {
		return "RBQ";
	}

}
