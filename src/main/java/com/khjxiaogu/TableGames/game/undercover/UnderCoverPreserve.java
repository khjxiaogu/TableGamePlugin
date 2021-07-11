package com.khjxiaogu.TableGames.game.undercover;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class UnderCoverPreserve extends PreserveInfo<UnderCoverGame> {

	public UnderCoverPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 7;
	}

	@Override
	protected int getMinMembers() {
		return 5;
	}

	@Override
	protected int getMaxMembers() {
		return 9;
	}
	static GameCreater<UnderCoverGame> gc=new DefaultGameCreater<>(UnderCoverGame.class);
	@Override
	protected GameCreater<UnderCoverGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "谁是卧底";
	}

}
