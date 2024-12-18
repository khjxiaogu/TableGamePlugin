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
package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;


public interface AbstractUser {
	void sendPrivate(String str);
	void sendPrivate(IMessage msg);
	void sendPublic(String str);
	void sendPublic(IMessage msg);
	void sendForName(String str);
	void sendForName(IMessage msg);
	void tryAvailable();
	IMessage getAt();
	String getMemberString();
	void setNameCard(String s);
	String getNameCard();
	AbstractRoom getRoom();
	void tryMute();
	void tryUnmute();
	UserIdentifier getId();
	UserIdentifier getHostId();
	void bind(Object obj);
	void setGame(Game g);
	default void registerListener(MessageListener msgc) {
		DynamicListeners.registerListener(getId(),getRoom().getId(),msgc);
	}
	default void releaseListener() {
		DynamicListeners.releaseListener(getId());
	}
	default void transferListener(AbstractUser another) {
		DynamicListeners.transferListener(getId(),another);
	}
	Object getRoleObject();
	Permission getPermission();
	boolean isFriend();
	default void tryMuteBackend() {
		
	}
	default void tryUnmuteBackend() {
		
	}
}
