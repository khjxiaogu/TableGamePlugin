package com.khjxiaogu.TableGames.game.chess.chara;

import com.khjxiaogu.TableGames.game.chess.CharacterInstance;
import com.khjxiaogu.TableGames.game.chess.CharacterPrototype;
import com.khjxiaogu.TableGames.game.chess.Characters;
import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Party;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;

public class BasicPrototype implements CharacterPrototype {
	int atk;
	int hp;
	Skill[] skills;
	public BasicPrototype(int atk, int hp, Skill... skills) {
		super();
		this.atk = atk;
		this.hp = hp;
		this.skills = skills;
	}
	@Override
	public Combatant createCharacter(Characters character,CharacterInstance instance, Party party) {
		SkillInstance[] sks=new SkillInstance[skills.length];
		if(sks.length>0)
			for(int i=0;i<sks.length;i++)
				sks[i]=new SkillInstance(skills[i]);
		return new Combatant(character,party,instance.extraAtk+atk,instance.extraHp+hp,sks);
	}

}
