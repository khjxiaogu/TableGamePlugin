package com.khjxiaogu.TableGames.game.chess;

public class CharacterInstance {
	public final Characters character;
	public int level;
	public int extraAtk;
	public int extraHp;
	public CharacterInstance(Characters character, int level) {
		super();
		this.character = character;
		this.level = level;
	}

	public Combatant createCharacter(Party party) {
		return character.createCharacter(this, party);
	}
}
