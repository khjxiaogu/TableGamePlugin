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
