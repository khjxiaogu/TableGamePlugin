package com.khjxiaogu.TableGames.game.idiomsolitare;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class SolitarePreserve extends PreserveInfo<IdiomSolitare> {

	public SolitarePreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 3;
	}

	@Override
	protected int getMinMembers() {
		return 2;
	}

	@Override
	protected int getMaxMembers() {
		return 5;
	}
	static GameCreater<IdiomSolitare> gc=new DefaultGameCreater<>(IdiomSolitare.class);
	@Override
	protected GameCreater<IdiomSolitare> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "成语接龙";
	}

}
