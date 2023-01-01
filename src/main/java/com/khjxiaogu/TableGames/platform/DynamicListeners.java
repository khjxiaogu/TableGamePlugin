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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.khjxiaogu.TableGames.platform.message.IMessageCompound;

public class DynamicListeners {

	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public UserIdentifier from;

		public MessageListenerWrapper(MessageListener ml,UserIdentifier from) {
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
		public  UserIdentifier from;

		public RoomMessageListenerWrapper(RoomMessageListener ml, UserIdentifier from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(AbstractUser u,IMessageCompound msg, MsgType type) {
			ml.handle(u,msg, type);
		}
	}
	public static ConcurrentHashMap<UserIdentifier, MessageListenerWrapper> mls = new ConcurrentHashMap<>();
	public static Map<Object,RoomMessageListenerWrapper> gls = new ConcurrentHashMap<>();
	
	public DynamicListeners() {
	}
	public static void registerListener(Object game,UserIdentifier roomid, RoomMessageListener ml) {
		gls.put(game,new RoomMessageListenerWrapper(ml, roomid));
	}
	public static void releaseListener(Object game) {
		gls.remove(game);
	}
	public static void registerListener(UserIdentifier uid,UserIdentifier roomid,MessageListener ml) {
		mls.put(uid, new MessageListenerWrapper(ml, roomid));
	}

	public static void registerListener(UserIdentifier uid, MessageListener ml) {
		mls.put(uid, new MessageListenerWrapper(ml,null));
	}


	public static void releaseListener(UserIdentifier uid) {
		mls.remove(uid);
	}
	public static void transferListener(UserIdentifier uid,AbstractUser id2) {
		MessageListenerWrapper ml = mls.remove(uid);
		if(ml!=null)
			id2.registerListener(ml);
	}
	public static boolean dispatch(UserIdentifier uid, MsgType type, IMessageCompound messageCompound) {
		
		MessageListenerWrapper ml = mls.get(uid);
		//System.out.println("dispatching " + uid);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(messageCompound, type);
		//System.out.println("dispatched " + uid);
		return true;
	}public static void dispatchAsync(UserIdentifier uid, MsgType type, IMessageCompound msg) {
		GlobalMain.dispatchexec.execute(()->DynamicListeners.dispatch(uid, type, msg));
	}
	public static void dispatchAsync(UserIdentifier uid,UserIdentifier rid,Supplier<AbstractUser> user, MsgType type, IMessageCompound msg) {
		GlobalMain.dispatchexec.execute(()->DynamicListeners.dispatch(uid, rid, user, type, msg));
	}
	public static boolean dispatch(UserIdentifier uid,UserIdentifier rid,Supplier<AbstractUser> user, MsgType type, IMessageCompound msg) {
		gls.values().stream().filter(e->e.from==rid).forEach(e->e.handle(user.get(),msg, type));
		
		MessageListenerWrapper ml = mls.get(uid);
		if (ml == null || !ml.isValid)
			return false;
		if (!(ml.from == null||rid.equals(ml.from)))
			return false;
		//System.out.println("dispatching msg to " + uid);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		mls.values().forEach(a -> a.isValid = false);
	}

}
