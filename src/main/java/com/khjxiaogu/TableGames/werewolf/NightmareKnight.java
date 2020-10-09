package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class NightmareKnight extends Werewolf {
	boolean isSkillUsed=false;
	public NightmareKnight(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public boolean shouldSurvive(DiedReason dir) {
		return true;
	}
	@Override
	public void doDaySkillPending(String s) {}
	@Override
	public void onTurn() {}
	@Override
	public void addDaySkillListener() {}
	@Override
	public String getRole() {
		return "恶灵骑士";
	}
}
