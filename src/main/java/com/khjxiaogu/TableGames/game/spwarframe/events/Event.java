/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
