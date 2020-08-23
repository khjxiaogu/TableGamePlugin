package com.khjxiaogu.TableGames;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.MessageListener.MsgType;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class Utils {
	static class MessageListenerWrapper implements MessageListener{
		public MessageListener ml;
		public boolean isValid=true;
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
	public static String getPlainText(MessageChain msg) {
		PlainText pt=msg.first(PlainText.Key);
		if(pt==null)
			return "";
		return pt.getContent().trim();
	}
	public static String removeLeadings(String leading,String orig) {
		return orig.replace(leading,"").trim();
	}
	public static void main(String[] args) {
		System.out.println(removeLeadings("保护","保护1"));
		System.out.println(removeLeadings("保护","保护1号"));
		System.out.println(removeLeadings("保护","保护 1"));
		System.out.println(removeLeadings("保护","保护 1号"));
	}
	static Map<Group,Game> gs=new ConcurrentHashMap<>();
	public static ConcurrentHashMap<Long,Utils.MessageListenerWrapper> mls=new ConcurrentHashMap<>();
	static Map<Group,Map<Class<? extends PreserveInfo<?>>,PreserveInfo<?>>> ps=new ConcurrentHashMap<>();
	@SuppressWarnings("unchecked")
	public static <T extends PreserveInfo<?>> T getPreserve(Group g,Class<T> type) {
		Map<Class<? extends PreserveInfo<?>>,PreserveInfo<?>> mc=ps.get(g);
		if(mc==null) {
			mc=new ConcurrentHashMap<>();
			ps.put(g,mc);
		}
		PreserveInfo<?> pi=mc.get(type);
		if(pi==null) {
			try {
				pi=type.getConstructor(Group.class).newInstance(g);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mc.put(type,pi);
		}
		return (T) pi;
	}
	public static void registerListener(Long id,Group g,MessageListener ml) {
		mls.put(id,new Utils.MessageListenerWrapper(ml,g));
	}
	public static void registerListener(Member m,MessageListener ml) {
		mls.put(m.getId(),new Utils.MessageListenerWrapper(ml,m.getGroup()));
	}
	public static void releaseListener(Long id) {
		mls.remove(id);
	}
	public static Set<Long> ingame=Collections.newSetFromMap(new ConcurrentHashMap<>());
	public static boolean tryAddMember(Long id) {
		return ingame.add(id);
	}
	public static boolean hasMember(Long id) {
		return ingame.contains(id);
	}
	public static void RemoveMember(Long id) {
		ingame.remove(id);
	}
	public static boolean dispatch(Long id,MsgType type,MessageChain msg) {
		synchronized(mls) {
			Utils.MessageListenerWrapper ml=mls.get(id);
			if(ml==null||!ml.isValid)return false;
			ml.handle(msg, type);
			return true;
		}
	}
	public static boolean dispatch(Long id,Group g,MsgType type,MessageChain msg) {
		synchronized(mls) {
			Utils.MessageListenerWrapper ml=mls.get(id);
			if(ml==null||!ml.isValid)return false;
			if(type!=MsgType.PRIVATE&&!g.equals(ml.from))return false;
			ml.handle(msg, type);
			return true;
		}
	}
	public static void InvalidListeners() {
		mls.values().iterator().forEachRemaining(a->a.isValid=false);
	}
	public static boolean hasActiveGame(Group gp) {
		Game g=gs.get(gp);
		if(g!=null&&g.isAlive())
			return true;
		return false;
	}
	public static <T extends Game> T createGame(Class<T> gameClass,Group gp,int count) {
		Game g=gs.get(gp);
		synchronized(gs) {
			if(g!=null&&g.isAlive()) {
				g.forceStop();
			}
			T ng=null;
			try {
				ng = gameClass.getConstructor(Group.class,int.class).newInstance(gp,count);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			gs.put(gp,ng);
			return ng;
		}
	}
}
