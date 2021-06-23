package com.khjxiaogu.TableGames.spwarframe.events;

import com.khjxiaogu.TableGames.spwarframe.GameManager;
import com.khjxiaogu.TableGames.spwarframe.role.Role;

public class ExposeEvent extends Event {
	Role target;
	public ExposeEvent(Role target) {
		this.target=target;
	}

	public Role getTarget() {
		return target;
	}

	public void setTarget(Role target) {
		this.target = target;
	}

	@Override
	protected void doExecute(GameManager room) {
		target.expose();
		room.sendAll(target.getPlayer()+"的阵营是"+target.getFraction().getName()+"，身份是"+target.getName());
		super.doExecute(room);
	}
	@Override
	public String toString() {
		return target.getName()+" 暴露了身份！";
	}
}
