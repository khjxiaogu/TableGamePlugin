package com.khjxiaogu.TableGames.spwarframe;

import java.util.function.Consumer;

import com.khjxiaogu.TableGames.spwarframe.events.Event;
import com.khjxiaogu.TableGames.spwarframe.role.Role;

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
