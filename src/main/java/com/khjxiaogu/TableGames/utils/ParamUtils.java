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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ParamUtils {
	private static Map<Class<?>,Function<String,?>> parsers=new HashMap<>();
	private static <T> void loadParser(Class<T> type,Function<String,T> parser) {
		ParamUtils.parsers.put(type,parser);
	}
	static {
		ParamUtils.loadParser(long.class,e->Long.parseLong(e));
		ParamUtils.loadParser(int.class,e->Integer.parseInt(e));
		ParamUtils.loadParser(String.class,e->e);
		ParamUtils.loadParser(double.class,e->Double.parseDouble(e));
		ParamUtils.loadParser(boolean.class,e->Boolean.parseBoolean(e));
		ParamUtils.loadParser(float.class,e->Float.parseFloat(e));
		ParamUtils.loadParser(char.class,e->(char)Integer.parseInt(e));
		ParamUtils.loadParser(short.class,e->Short.parseShort(e));
	}
	public static List<String> loadParams(Object ob) {
		List<String> ret=new ArrayList<>();
		Class<?> c=ob.getClass();
		while(c!=null) {
			for(Field f:c.getDeclaredFields()) {
				if(ParamUtils.parsers.containsKey(f.getType())) {
					ret.add(f.getName());
				}
			}
			c=c.getSuperclass();
		}
		return ret;
	}
	public static String getValue(Object ob,String p) {
		Class<?> c=ob.getClass();
		outer:
			while(c!=Object.class&&c!=null) {
				for(Field f:c.getDeclaredFields()) {
					if(f.getName().equals(p)) {
						f.setAccessible(true);
						try {
							return String.valueOf(f.get(ob));
						} catch (IllegalArgumentException | IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break outer;
					}
				}
				c=c.getSuperclass();
			}
		return "无效";
	}
	public static boolean setValue(Object ob,String p,String v) {
		Class<?> c=ob.getClass();
		outer:
			while(c!=Object.class&&c!=null) {
				for(Field f:c.getDeclaredFields()) {
					if(f.getName().equals(p)) {
						Function<String,?> parser=ParamUtils.parsers.get(f.getType());
						if(parser!=null) {
							f.setAccessible(true);
							try {
								f.set(ob,parser.apply(v));
								return true;
							} catch (IllegalArgumentException | IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						break outer;
					}
				}
				c=c.getSuperclass();
			}
		return false;
	}
}
