package com.khjxiaogu.TableGames.game.spwarframe.events;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class DeadAnnounceEvent extends Event {
	private int counter=0;
	private Role target;
	public DeadAnnounceEvent(Role target) { super();this.target=target; }
	public void addCounter() {
		counter++;
	}
	public void addCounter(int c) {
		counter+=c;
	}
	public void setCounter(int counter) {
		this.counter = counter;
	}
	public Role getTarget() {
		return target;
	}
	public int getCounter() { return counter; }
	@Override
	public String toString() {
		return target.getName()+"死亡，被杀次数："+counter;
	}
}
