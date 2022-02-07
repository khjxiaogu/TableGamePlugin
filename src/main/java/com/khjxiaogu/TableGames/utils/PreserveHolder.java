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
