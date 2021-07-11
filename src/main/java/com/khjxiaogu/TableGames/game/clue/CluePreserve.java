package com.khjxiaogu.TableGames.game.clue;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
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
	static GameCreater<ClueGame> gc=new DefaultGameCreater<>(ClueGame.class);
	@Override
	protected GameCreater<ClueGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "妙探寻凶";
	}

}
