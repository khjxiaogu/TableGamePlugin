package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class NightmareKnight extends Werewolf {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 1.5;
	}

	@Override
	public double onSkilledAccuracy() {
		return 1.1;
	}

	@Override
	public String getJobDescription() {
		return "你属于狼阵营，你可以与其他狼人一起杀人，但是你不会在晚上死亡。并且在第一次被使用技能时会反伤杀死发动技能者。";
	}

	public NightmareKnight(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	boolean isSkillUsed = false;

	public NightmareKnight(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public boolean shouldSurvive(DiedReason dir) {
		return true;
	}

	@Override
	public void doDaySkillPending(String s) {
	}

	@Override
	public void onTurn() {
	}

	@Override
	public void addDaySkillListener() {
	}

	@Override
	public String getRole() {
		return "恶灵骑士";
	}
}
