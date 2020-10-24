package com.khjxiaogu.TableGames.fastclue;

import com.khjxiaogu.TableGames.fastclue.Card.CardType;

public class Room {
	Room next;
	String name;
	Card present;
	public Room(String name,int id) {
		this.name = name;
		present=new Card(name,id,CardType.Room);
	}
}
