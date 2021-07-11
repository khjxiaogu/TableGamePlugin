package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class StandardWerewolfPreserve extends PreserveInfo<WerewolfGame>{

	public StandardWerewolfPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 12;
	}

	@Override
	protected int getMinMembers() {
		return 12;
	}

	@Override
	protected int getMaxMembers() {
		return 12;
	}
	static GameCreater<WerewolfGame> gc=new StandardWerewolfCreater();
	@Override
	protected GameCreater<WerewolfGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "标准狼人杀";
	}

}
