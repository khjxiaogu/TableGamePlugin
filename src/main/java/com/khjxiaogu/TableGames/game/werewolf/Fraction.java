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
package com.khjxiaogu.TableGames.game.werewolf;

import java.util.HashMap;

public enum Fraction {
	
	God("神"), Innocent("民"), Wolf("狼"), Other("三");

	String name;
	private static final HashMap<String,Fraction> names=new HashMap<>();
	private Fraction(String name) {
		this.name = name;
		
	}
	static {
		for(Fraction f:values())
		names.put(f.name, f);
	}
	public static Fraction getByName(String name) {
		return names.get(name);
	}
}
