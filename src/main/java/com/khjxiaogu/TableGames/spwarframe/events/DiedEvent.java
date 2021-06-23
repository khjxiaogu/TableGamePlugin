package com.khjxiaogu.TableGames.spwarframe.events;

import java.util.LinkedList;
import java.util.List;

import com.khjxiaogu.TableGames.spwarframe.role.Role;

public class DiedEvent extends Event {
	private List<KillEvent> killBy=new LinkedList<>();
	private Role target;
	public DiedEvent(Role target) { super();this.target=target; }
	public DiedEvent(Role target,KillEvent by) { super();this.target=target;killBy.add(by); }
	public DiedEvent(Role target, List<KillEvent> by) {
		this.target=target;
		killBy.addAll(by);
	}
	public Role getTarget() {
		return target;
	}
	public List<KillEvent> getKillBy() {
		return killBy;
	}
	public void populateKill(KillEvent ev) {
		killBy.add(ev);
	}
	@Override
	public String toString() {
		return target.getName()+" 被杀了。";
	}
}
