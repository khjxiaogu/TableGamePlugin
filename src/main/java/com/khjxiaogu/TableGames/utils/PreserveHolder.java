package com.khjxiaogu.TableGames.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.mamoe.mirai.contact.Group;

public class PreserveHolder {

	static Map<Group, Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>>> ps = new ConcurrentHashMap<>();

	public PreserveHolder() {
	}

	@SuppressWarnings("unchecked")
	public static <T extends PreserveInfo<?>> T getPreserve(Group g, Class<T> type) {
		Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>> mc = PreserveHolder.ps.get(g);
		if (mc == null) {
			mc = new ConcurrentHashMap<>();
			PreserveHolder.ps.put(g, mc);
		}
		PreserveInfo<?> pi = mc.get(type);
		if (pi == null) {
			try {
				pi = type.getConstructor(Group.class).newInstance(g);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mc.put(type, pi);
		}
		return (T) pi;
	}

}
