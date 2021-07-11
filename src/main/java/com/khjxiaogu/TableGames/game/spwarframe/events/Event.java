package com.khjxiaogu.TableGames.game.spwarframe.events;

import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public class Event {
	public enum Priority{
		KillEvent,
		SkillEvent,

	}
	private boolean canceled;
	private boolean rejected;
	public int priority;
	private BiConsumer<GameManager,Event> doskill;
	static long es=0;
	public synchronized long newEID() {
		return ++Event.es;
	}
	public final long eid;
	public boolean isRejected() { return rejected; }
	public void setRejected(boolean rejected) { this.rejected = rejected; }
	public void reject() {rejected=true;}
	public boolean isCanceled() { return canceled; }
	public void setCanceled(boolean canceled) { this.canceled = canceled; }
	public void cancel() {canceled=true;}
	public Event() {

		eid=newEID();
	}
	public Event andThen(BiConsumer<GameManager,Event> doskill) {
		if(this.doskill==null) {
			this.doskill = doskill;
		} else {
			this.doskill.andThen(doskill);
		}
		return this;
	}

	public void executeEvent(GameManager room) {
		doExecute(room);
	}
	protected void doExecute(GameManager room) {
		if(doskill!=null) {
			doskill.accept(room,this);
		}
	}
	/**
	 * @param role
	 */
	public boolean isTargeting(Role role) {
		return false;
	}
	public Event setPriority(int level) {
		priority=level;
		return this;
	}
	public int getPriority() {
		return priority;
	}
}
