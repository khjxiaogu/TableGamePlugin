package com.khjxiaogu.TableGames.game.chess.skills;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Party;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;
import com.khjxiaogu.TableGames.game.chess.effects.Strength;

public class BufferSkill implements Skill{

	public BufferSkill() {
	}


	@Override
	public void battleStart(SkillInstance instance, Combatant owner, Party opponent) {
		Skill.super.battleStart(instance, owner, opponent);
		instance.usedTime++;
		int max=Integer.MIN_VALUE;
		Combatant toAtk=null;
		for(Combatant comba:owner.party.list) {
			if(comba.attack>max) {
				toAtk=comba;
				max=comba.attack;
			}
		};
		toAtk.attack+=1;
		toAtk.addEffectLevel(null, Strength.INSTANCE, 1);
	}

	@Override
	public String getDisplayText(SkillInstance instance) {
		return "入场时，己方攻击力最高的棋子获得1攻击1力量。";
	}

	@Override
	public String getStatusText(SkillInstance instance) {
		return "增效";
	}

}
