package com.khjxiaogu.TableGames.game.werewolf;

public enum Fraction {
	God("神"), Innocent("民"), Wolf("狼"),Other("三");
	String name;

	private Fraction(String name) {
		this.name = name;
	}
}
