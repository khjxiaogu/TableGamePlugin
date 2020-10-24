package com.khjxiaogu.TableGames.clue;

public class RoleCard extends Card {
	ClueGame game;
	public RoleCard(ClueGame g,int num) {
		super("",num,CardType.Role);
		game=g;
	}
	@Override
	String getName() {
		return game.getPlayer(id).member.getNameCard();
	}
	
}
