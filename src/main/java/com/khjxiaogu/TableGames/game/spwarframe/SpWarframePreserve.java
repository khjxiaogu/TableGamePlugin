package com.khjxiaogu.TableGames.game.spwarframe;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class SpWarframePreserve extends PreserveInfo<SpWarframe> {

	public SpWarframePreserve(AbstractRoom g) {
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
	static GameCreater<SpWarframe> gc=new DefaultGameCreater<>(SpWarframe.class);
	@Override
	protected GameCreater<SpWarframe> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "SP战纪";
	}

}
