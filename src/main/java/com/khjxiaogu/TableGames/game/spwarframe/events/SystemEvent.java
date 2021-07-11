package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;

public class SystemEvent extends Event {
	public GameTurn toExecute;
	public SystemEvent(GameTurn toExecute) {
		super();
		this.toExecute=toExecute;
	}

}
