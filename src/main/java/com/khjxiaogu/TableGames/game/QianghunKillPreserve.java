package com.khjxiaogu.TableGames.game;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;

public class QianghunKillPreserve extends PreserveInfo<QianghunKill> {

	public QianghunKillPreserve(AbstractRoom g) {
		super(g);
		// TODO Auto-generated constructor stub
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
		return 100;
	}
	static GameCreater<QianghunKill> gc=new DefaultGameCreater<>(QianghunKill.class);
	@Override
	protected GameCreater<QianghunKill> getGameClass() {
		return gc;
	}

	@Override
	public String getName() {
		return "枪魂杀";
	}

}
