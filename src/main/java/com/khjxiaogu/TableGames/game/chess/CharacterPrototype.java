package com.khjxiaogu.TableGames.game.chess;

public interface CharacterPrototype {
	Combatant createCharacter(Characters character,CharacterInstance instance,Party party);
}
