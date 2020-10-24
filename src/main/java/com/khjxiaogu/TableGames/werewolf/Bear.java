package com.khjxiaogu.TableGames.werewolf;

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
		game.logger.logRaw("熊没有咆哮");
		game.sendPublicMessage("昨晚熊没有咆哮。");
	}
	@Override
	public String getRole() {
		return "熊";
	}
	@Override
	public int getTurn() {
		return 4;
	}
	public void sendWolf() {
		game.logger.logRaw("熊咆哮了");
		game.sendPublicMessage("昨晚熊咆哮了。");
	}
}
