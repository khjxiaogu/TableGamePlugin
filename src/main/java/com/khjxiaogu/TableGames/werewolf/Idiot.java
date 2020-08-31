package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class Idiot extends Villager {
	public Idiot(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	boolean canVote=true;
	@Override
	public void vote() {
		if(canVote)
			super.vote();
	}

	@Override
	public void onDied(DiedReason dir) {
		if(dir!=DiedReason.Vote) {
			super.onDied(dir);
		}else {
			isDead=false;
			canVote=false;
			sendPublic("被驱逐了，身份是白痴。");
		}
	}
	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}
	@Override
	public String getRole() {
		return "白痴";
	}
}
