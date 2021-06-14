package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager.GameTurn;

public class SystemEvent extends Event {
	public GameTurn toExecute;
	public SystemEvent(GameTurn toExecute) {
		super();
		this.toExecute=toExecute;
	}

}
