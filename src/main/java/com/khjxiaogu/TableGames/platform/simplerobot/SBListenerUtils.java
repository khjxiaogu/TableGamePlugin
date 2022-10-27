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
package com.khjxiaogu.TableGames.platform.simplerobot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.RoomMessageListener;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import love.forte.simbot.ID;
import love.forte.simbot.definition.Channel;
import love.forte.simbot.definition.GuildMember;

public class SBListenerUtils {

	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public ID from;

		public MessageListenerWrapper(MessageListener ml,ID from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(IMessageCompound msg, MsgType type) {
			ml.handle(msg, type);
		}
	}
	static class RoomMessageListenerWrapper implements RoomMessageListener {
		public RoomMessageListener ml;
		public boolean isValid = true;
		public ID from;

		public RoomMessageListenerWrapper(RoomMessageListener ml,ID from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(AbstractUser u,IMessageCompound msg, MsgType type) {
			ml.handle(u,msg, type);
		}
	}
	public static ConcurrentHashMap<ID, MessageListenerWrapper> mls = new ConcurrentHashMap<>();
	public static Map<Object,RoomMessageListenerWrapper> gls = new ConcurrentHashMap<>();
	
	public SBListenerUtils() {
	}
	public static void registerListener(Object game,Channel g, RoomMessageListener ml) {
		gls.put(game,new RoomMessageListenerWrapper(ml, g.getId()));
	}
	public static void releaseListener(Object game) {
		gls.remove(game);
	}
	public static void registerListener(ID id, Channel g, MessageListener ml) {
		mls.put(id, new MessageListenerWrapper(ml, g.getId()));
	}

	public static void registerListener(ID id, MessageListener ml) {
		mls.put(id, new MessageListenerWrapper(ml,null));
	}

	public static void registerListener(GuildMember m,Channel c, MessageListener ml) {
		mls.put(m.getId(), new MessageListenerWrapper(ml, c.getId()));
	}

	public static void releaseListener(ID id) {
		mls.remove(id);
	}
	public static void transferListener(ID id,AbstractUser id2) {
		MessageListenerWrapper ml = mls.remove(id);
		if(ml!=null)
			id2.registerListener(ml);
	}
	public static boolean dispatch(ID id, MsgType type, IMessageCompound messageCompound) {
		
		MessageListenerWrapper ml = mls.get(id);
		//System.out.println("dispatching " + id);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(messageCompound, type);
		//System.out.println("dispatched " + id);
		return true;
	}

	public static boolean dispatch(GuildMember m,Channel g, MsgType type, IMessageCompound msg) {
		gls.values().stream().filter(e->e.from==g.getId()).forEach(e->e.handle(new SBHumanUser(m,g),msg, type));
		
		MessageListenerWrapper ml = mls.get(m.getId());
		if (ml == null || !ml.isValid)
			return false;
		if (!(ml.from == null||g.getId().equals(ml.from)))
			return false;
		//System.out.println("dispatching msg to " + id);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		mls.values().forEach(a -> a.isValid = false);
	}

}
