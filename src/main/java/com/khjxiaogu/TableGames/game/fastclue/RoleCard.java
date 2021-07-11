package com.khjxiaogu.TableGames.game.fastclue;

public class RoleCard extends Card {
	FastClueGame game;
	public RoleCard(FastClueGame g,int num) {
		super("",num,CardType.Role);
		game=g;
	}
	@Override
	String getName() {
		return game.getPlayer(id).getNameCard();
	}

}
