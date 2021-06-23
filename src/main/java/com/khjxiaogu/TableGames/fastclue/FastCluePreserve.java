package com.khjxiaogu.TableGames.fastclue;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class FastCluePreserve extends PreserveInfo<FastClueGame> {

	public FastCluePreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 6;
	}

	@Override
	protected int getMinMembers() {
		return 3;
	}

	@Override
	protected int getMaxMembers() {
		return 6;
	}

	@Override
	protected Class<FastClueGame> getGameClass() {
		return FastClueGame.class;
	}

	@Override
	public String getName() {
		return "妙探寻凶X";
	}

}
