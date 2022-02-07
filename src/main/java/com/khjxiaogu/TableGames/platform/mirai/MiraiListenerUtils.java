/**
 * Mirai Song Plugin
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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.platform.mirai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.RoomMessageListener;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;

public class MiraiListenerUtils {

	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public long from;

		public MessageListenerWrapper(MessageListener ml,long from) {
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
		public long from;

		public RoomMessageListenerWrapper(RoomMessageListener ml,long from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(AbstractUser u,IMessageCompound msg, MsgType type) {
			ml.handle(u,msg, type);
		}
	}
	public static ConcurrentHashMap<Long, MessageListenerWrapper> mls = new ConcurrentHashMap<>();
	public static List<RoomMessageListenerWrapper> gls = Collections.synchronizedList(new ArrayList<>());
	
	public MiraiListenerUtils() {
	}
	public static void registerListener(Group g, RoomMessageListener ml) {
		gls.add(new RoomMessageListenerWrapper(ml, g.getId()));
	}
	public static void releaseListener(Group g) {
		gls.removeIf(w->w.from==g.getId());
	}
	public static void registerListener(Long id, Group g, MessageListener ml) {
		mls.put(id, new MessageListenerWrapper(ml, g.getId()));
	}

	public static void registerListener(Long id, MessageListener ml) {
		mls.put(id, new MessageListenerWrapper(ml,0));
	}

	public static void registerListener(Member m, MessageListener ml) {
		mls.put(m.getId(), new MessageListenerWrapper(ml, m.getGroup().getId()));
	}

	public static void releaseListener(Long id) {
		mls.remove(id);
	}

	public static boolean dispatch(Long id, MsgType type, IMessageCompound messageCompound) {
		
		MessageListenerWrapper ml = mls.get(id);
		//System.out.println("dispatching " + id);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(messageCompound, type);
		//System.out.println("dispatched " + id);
		return true;
	}

	public static boolean dispatch(Member m, Group g, MsgType type, IMessageCompound msg) {
		gls.stream().filter(e->e.from==g.getId()).forEach(e->e.handle(new MiraiHumanUser((NormalMember)m),msg, type));
		
		MessageListenerWrapper ml = mls.get(m.getId());
		if (ml == null || !ml.isValid)
			return false;
		if (!(ml.from == 0||g.getId()==ml.from))
			return false;
		//System.out.println("dispatching msg to " + id);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		mls.values().forEach(a -> a.isValid = false);
	}

}
