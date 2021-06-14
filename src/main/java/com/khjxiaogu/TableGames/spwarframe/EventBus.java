package com.khjxiaogu.TableGames.spwarframe;

import java.util.function.Consumer;

import com.khjxiaogu.TableGames.spwarframe.events.Event;
import com.khjxiaogu.TableGames.spwarframe.role.Role;

public interface EventBus {
	public <T extends Event> void RegisterListener(Role of,Class<T> ev,Consumer<T> listener) ;
	public <T extends Event> void RegisterListener(Class<T> ev,Consumer<T> listener) ;
	public void RegisterListener(Role of,Consumer<Event> listener) ;
	public void RegisterListener(Consumer<Event> listener) ;
	public <T extends Event> void HookEvents(Role of,Class<T> ev,Consumer<T> hook) ;
	public <T extends Event> void HookEvents(Class<T> ev,Consumer<T> hook) ;
	public void HookEvents(Role of,Consumer<Event> hook) ;
	public void HookEvents(Consumer<Event> hook) ;
	public void RemoveListener(Consumer<? extends Event> listener) ;
	public void CheckEvents(Consumer<Event> checker) ;
	public <T extends Event> void CheckEvents(Class<T> ev,Consumer<T> checker) ;
	public void fireEventLater(Event ev) ;
	public boolean fireEvent(Event ev);
}
