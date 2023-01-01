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


public interface AbstractRoom {
	AbstractUser getOwner();
	AbstractUser get(UserIdentifier id);
	void sendMessage(IMessage msg);
	void sendMessage(String msg);
	Object getInstance();
	default void registerRoomListener(Object game,RoomMessageListener ml) {
		DynamicListeners.registerListener(game,getId(), ml);
	}
	default void releaseRoomListener(Object game) {
		DynamicListeners.releaseListener(game);
	}
	default void registerListener(UserIdentifier id, MessageListener ml) {
		DynamicListeners.registerListener(id,getId(), ml);
	}
	default void releaseListener(UserIdentifier id) {
		DynamicListeners.releaseListener(id);
	}
	void setMuteAll(boolean isMute);
	String getHostNameCard();
	UserIdentifier getId();
	AbstractBotUser createBot(int id,Class<? extends BotUserLogic> logicCls,Game in);
}
