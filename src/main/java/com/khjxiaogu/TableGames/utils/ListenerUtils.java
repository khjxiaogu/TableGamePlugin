package com.khjxiaogu.TableGames.utils;

import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class ListenerUtils {

	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public Group from;
	
		public MessageListenerWrapper(MessageListener ml, Group from) {
			this.ml = ml;
			this.from = from;
		}
	
		@Override
		public void handle(MessageChain msg, MsgType type) {
			ml.handle(msg, type);
		}
	}

	public static ConcurrentHashMap<Long, MessageListenerWrapper> mls = new ConcurrentHashMap<>();

	public ListenerUtils() {
	}

	public static void registerListener(Long id, Group g, MessageListener ml) {
		mls.put(id, new ListenerUtils.MessageListenerWrapper(ml, g));
	}

	public static void registerListener(Long id, MessageListener ml) {
		mls.put(id, new ListenerUtils.MessageListenerWrapper(ml, null));
	}

	public static void registerListener(Member m, MessageListener ml) {
		mls.put(m.getId(), new ListenerUtils.MessageListenerWrapper(ml, m.getGroup()));
	}

	public static void releaseListener(Long id) {
		mls.remove(id);
	}

	public static boolean dispatch(Long id, MsgType type, MessageChain msg) {
		if (Utils.getPlainText(msg).startsWith("重置")) {
			for (Game g : GameUtils.getGames().values()) {
				if (g.isAlive())
					if (g.onReAttach(id))
						break;
			}
			return true;
		}
		ListenerUtils.MessageListenerWrapper ml = mls.get(id);
		System.out.println("dispatching " + id);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(msg, type);
		System.out.println("dispatched " + id);
		return true;
	}

	public static boolean dispatch(Long id, Group g, MsgType type, MessageChain msg) {
		ListenerUtils.MessageListenerWrapper ml = mls.get(id);
		if (ml == null || !ml.isValid)
			return false;
		if (!(ml.from == null||g.equals(ml.from)))
			return false;
		System.out.println("dispatching msg to " + id);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		mls.values().iterator().forEachRemaining(a -> a.isValid = false);
	}

}
