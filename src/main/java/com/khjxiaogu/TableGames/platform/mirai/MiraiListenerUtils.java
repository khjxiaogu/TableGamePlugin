package com.khjxiaogu.TableGames.platform.mirai;

import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.MessageListener;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class MiraiListenerUtils {

	static class MessageListenerWrapper implements MessageListener {
		public MessageListener ml;
		public boolean isValid = true;
		public Group from;

		public MessageListenerWrapper(MessageListener ml, Group from) {
			this.ml = ml;
			this.from = from;
		}

		@Override
		public void handle(IMessageCompound msg, MsgType type) {
			ml.handle(msg, type);
		}
	}

	public static ConcurrentHashMap<Long, MessageListenerWrapper> mls = new ConcurrentHashMap<>();

	public MiraiListenerUtils() {
	}

	public static void registerListener(Long id, Group g, MessageListener ml) {
		MiraiListenerUtils.mls.put(id, new MiraiListenerUtils.MessageListenerWrapper(ml, g));
	}

	public static void registerListener(Long id, MessageListener ml) {
		MiraiListenerUtils.mls.put(id, new MiraiListenerUtils.MessageListenerWrapper(ml, null));
	}

	public static void registerListener(Member m, MessageListener ml) {
		MiraiListenerUtils.mls.put(m.getId(), new MiraiListenerUtils.MessageListenerWrapper(ml, m.getGroup()));
	}

	public static void releaseListener(Long id) {
		MiraiListenerUtils.mls.remove(id);
	}

	public static boolean dispatch(Long id, MsgType type, IMessageCompound messageCompound) {
		if (messageCompound.getText().startsWith("重置")) {
			for (Game g : GameUtils.getGames().values()) {
				if (g.isAlive())
					if (g.onReAttach(id)) {
						break;
					}
			}
			return true;
		}
		MiraiListenerUtils.MessageListenerWrapper ml = MiraiListenerUtils.mls.get(id);
		System.out.println("dispatching " + id);
		if (ml == null || !ml.isValid)
			return false;
		ml.handle(messageCompound, type);
		System.out.println("dispatched " + id);
		return true;
	}

	public static boolean dispatch(Long id, Group g, MsgType type, IMessageCompound msg) {
		MiraiListenerUtils.MessageListenerWrapper ml = MiraiListenerUtils.mls.get(id);
		if (ml == null || !ml.isValid)
			return false;
		if (!(ml.from == null||g.equals(ml.from)))
			return false;
		System.out.println("dispatching msg to " + id);
		ml.handle(msg, type);
		return true;
	}

	public static void InvalidListeners() {
		MiraiListenerUtils.mls.values().iterator().forEachRemaining(a -> a.isValid = false);
	}

}
