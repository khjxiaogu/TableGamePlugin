package com.khjxiaogu.TableGames.game.chess;

import com.khjxiaogu.TableGames.game.chess.chara.BasicPrototype;
import com.khjxiaogu.TableGames.game.chess.skills.AssassinSkill;
import com.khjxiaogu.TableGames.game.chess.skills.BufferSkill;
import com.khjxiaogu.TableGames.game.chess.skills.HealerSkill;
import com.khjxiaogu.TableGames.game.chess.skills.ShielderSkill;
import com.khjxiaogu.TableGames.game.chess.skills.WarriorSkill;

public enum Characters{
	SHIELDER(1,10,1,new ShielderSkill()),
	WARRIOR(3,8,1,new WarriorSkill()),
	HEALER(1,9,1,new HealerSkill()),
	ASSASSIN(4,6,1,new AssassinSkill()),
	BUFFER(0,14,1,new BufferSkill());
	public final CharacterPrototype delegate;
	public final int rarity;
	private Characters(CharacterPrototype delegate,int rarity) {
		this.delegate = delegate;
		this.rarity=rarity;
	}
	private Characters(int atk, int hp,int rarity, Skill... skills) {
		this.delegate = new BasicPrototype(atk,hp,skills);
		this.rarity=rarity;
	}

	public Combatant createCharacter(CharacterInstance instance, Party party) {
		return delegate.createCharacter(this,instance, party);
	}
}
