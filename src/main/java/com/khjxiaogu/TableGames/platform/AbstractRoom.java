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


public interface AbstractRoom {
	AbstractUser getOwner();
	AbstractUser get(UserIdentifier id);
	void sendMessage(IMessage msg);
	void sendMessage(String msg);
	Object getInstance();
	void registerRoomListener(RoomMessageListener ml);
	void registerListener(UserIdentifier id, MessageListener ml);
	void releaseListener(UserIdentifier id);
	void setMuteAll(boolean isMute);
	String getHostNameCard();
	UserIdentifier getId();
	void releaseRoomListener();
}
