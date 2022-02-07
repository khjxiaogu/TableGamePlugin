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
package com.khjxiaogu.TableGames.game.spwarframe;

import java.util.function.Consumer;

import com.khjxiaogu.TableGames.game.spwarframe.events.Event;
import com.khjxiaogu.TableGames.game.spwarframe.role.Role;

public interface EventBus {
	<T extends Event> void RegisterListener(Role of,Class<T> ev,Consumer<T> listener) ;
	<T extends Event> void RegisterListener(Class<T> ev,Consumer<T> listener) ;
	void RegisterListener(Role of,Consumer<Event> listener) ;
	void RegisterListener(Consumer<Event> listener) ;
	<T extends Event> void HookEvents(Role of,Class<T> ev,Consumer<T> hook) ;
	<T extends Event> void HookEvents(Class<T> ev,Consumer<T> hook) ;
	void HookEvents(Role of,Consumer<Event> hook) ;
	void HookEvents(Consumer<Event> hook) ;
	void RemoveListener(Consumer<? extends Event> listener) ;
	void CheckEvents(Consumer<Event> checker) ;
	<T extends Event> void CheckEvents(Class<T> ev,Consumer<T> checker) ;
	void fireEventLater(Event ev) ;
	boolean fireEvent(Event ev);
}
