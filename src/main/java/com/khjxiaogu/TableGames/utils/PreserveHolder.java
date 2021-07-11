package com.khjxiaogu.TableGames.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;


public class PreserveHolder {

	static Map<AbstractRoom, Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>>> ps = new ConcurrentHashMap<>();

	public PreserveHolder() {
	}

	@SuppressWarnings("unchecked")
	public static <T extends PreserveInfo<?>> T getPreserve(AbstractRoom g, Class<T> type) {
		Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>> mc = PreserveHolder.ps.get(g);
		if (mc == null) {
			mc = new ConcurrentHashMap<>();
			PreserveHolder.ps.put(g, mc);
		}
		PreserveInfo<?> pi = mc.get(type);
		if (pi == null) {
			try {
				pi = type.getConstructor(AbstractRoom.class).newInstance(g);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mc.put(type, pi);
		}
		return (T) pi;
	}
	@SuppressWarnings("unchecked")
	public static <T extends PreserveInfo<?>> List<T> getPreserves(AbstractUser u, Class<T> type) {
		List<T> pivs=new ArrayList<>();
		for(Map<Class<? extends PreserveInfo<?>>, PreserveInfo<?>> mc:PreserveHolder.ps.values()) {
			PreserveInfo<?> pi = mc.get(type);
			if(pi!=null&&pi.hasPreserver(u)) {
				pivs.add((T) pi);
			}
		}
		
		return pivs;
	}
}
