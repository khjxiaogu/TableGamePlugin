package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractRoom;


public class MiniWerewolfPreserve extends WerewolfPreserve{

	public MiniWerewolfPreserve(AbstractRoom g) {
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
		return 12;
	}
	@Override
	public String getName() {
		return "小型狼人杀";
	}

}
