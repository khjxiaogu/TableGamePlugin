package com.khjxiaogu.TableGames.spwarframe;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
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

	@Override
	protected Class<SpWarframe> getGameClass() {
		return SpWarframe.class;
	}

	@Override
	public String getName() {
		return "SP战纪";
	}

}
