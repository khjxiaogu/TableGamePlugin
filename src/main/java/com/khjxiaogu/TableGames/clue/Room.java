package com.khjxiaogu.TableGames.clue;

import com.khjxiaogu.TableGames.clue.Card.CardType;

public class Room {
	Room next;
	String name;
	Card present;
	public Room(String name,int id) {
		this.name = name;
		present=new Card(name,id,CardType.Room);
	}
}
