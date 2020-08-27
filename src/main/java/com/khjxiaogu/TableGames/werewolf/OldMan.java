package com.khjxiaogu.TableGames.werewolf;

import net.mamoe.mirai.contact.Member;

public class OldMan extends Innocent {
	boolean lifeUsed=false;
	public OldMan(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}
	@Override
	public void onGameStart() {
		super.onGameStart();
		super.sendPrivate("你有两条命，可以被狼人杀两次，但是被毒或者驱逐也会死。");
	}
	@Override
	public String getRole() {
		return "长老";
	}

}
