package com.khjxiaogu.TableGames.clue;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class CluePreserve extends PreserveInfo<ClueGame> {

	public CluePreserve(AbstractRoom g) {
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
	protected Class<ClueGame> getGameClass() {
		return ClueGame.class;
	}

	@Override
	public String getName() {
		return "妙探寻凶";
	}

}
