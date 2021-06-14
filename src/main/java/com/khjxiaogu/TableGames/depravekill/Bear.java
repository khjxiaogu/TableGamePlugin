package com.khjxiaogu.TableGames.depravekill;

import net.mamoe.mirai.contact.Member;

public class Bear extends Villager {

	public Bear(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}
	@Override
	public void onTurn() {
		Villager pt=this;
		while(true) {
			pt=pt.prev;
			if(pt.isDead)continue;
			if(pt.getFraction()==Fraction.Wolf) {sendWolf();return;}
			break;
		}
		pt=this;
		while(true) {
			pt=pt.next;
			if(pt.isDead)continue;
			if(pt.getFraction()==Fraction.Wolf) {sendWolf();return;}
			break;
		}
		sendNonWolf();
	}
	@Override
	public String getRole() {
		return "犬少女";
	}
	@Override
	public int getTurn() {
		return 4;
	}
	public void sendNonWolf() {
		game.logger.logRaw("犬少女没有吠叫");
		game.sendPublicMessage("昨晚犬少女没有吠叫。");
	}
	public void sendWolf() {
		game.logger.logRaw("犬少女吠叫了");
		game.sendPublicMessage("昨晚犬少女吠叫了。");
	}
}
