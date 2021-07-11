package com.khjxiaogu.TableGames.game.fastclue;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
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
	static GameCreater<FastClueGame> gc=new DefaultGameCreater<>(FastClueGame.class);
	@Override
	protected GameCreater<FastClueGame> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "妙探寻凶X";
	}

}
