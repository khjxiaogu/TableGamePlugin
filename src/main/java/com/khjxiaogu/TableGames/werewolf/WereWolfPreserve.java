package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.PreserveInfo;

import net.mamoe.mirai.contact.Group;

public class WerewolfPreserve extends PreserveInfo<WerewolfGame> {

	public WerewolfPreserve(Group g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 9;
	}

	@Override
	protected int getMinMembers() {
		return 6;
	}

	@Override
	protected int getMaxMembers() {
		return 18;
	}

	@Override
	protected Class<WerewolfGame> getGameClass() {
		return WerewolfGame.class;
	}

	@Override
	public String getName() {
		return "狼人杀";
	}

}
