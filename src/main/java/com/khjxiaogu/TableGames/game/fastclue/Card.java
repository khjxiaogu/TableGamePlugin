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
package com.khjxiaogu.TableGames.game.fastclue;

public class Card {
	String name;
	public enum CardType{
		Weapon("武器"),
		Room("房间"),
		Role("角色");
		String desc;

		private CardType(String desc) {
			this.desc = desc;
		}
	}
	CardType type;
	int id;

	public Card(String name,int id, CardType type) {
		super();
		this.name = name;
		this.type = type;
		this.id=id;
	}


	public String getDisplayName() {
		return type.desc+":"+id+"、"+getName();
	}


	String getName() {
		return name;
	}
}
