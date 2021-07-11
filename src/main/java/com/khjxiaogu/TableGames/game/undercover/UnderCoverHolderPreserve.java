package com.khjxiaogu.TableGames.game.undercover;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class UnderCoverHolderPreserve extends PreserveInfo<UnderCoverHolder> {

	public UnderCoverHolderPreserve(AbstractRoom g) {
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
		return 14;
	}
	static GameCreater<UnderCoverHolder> gc=new DefaultGameCreater<>(UnderCoverHolder.class);
	@Override
	protected GameCreater<UnderCoverHolder> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "谁是卧底发词";
	}

}
