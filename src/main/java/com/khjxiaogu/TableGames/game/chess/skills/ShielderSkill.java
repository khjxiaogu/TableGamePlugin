package com.khjxiaogu.TableGames.game.chess.skills;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.EffectInstance;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;
import com.khjxiaogu.TableGames.game.chess.effects.Shield;

public class ShielderSkill implements Skill{

	public ShielderSkill() {
	}

	@Override
	public void beforeAttack(SkillInstance instance, Combatant owner, Combatant character) {
		Skill.super.beforeAttack(instance, owner, character);
		instance.usedTime++;
		owner.addEffectLevel(new EffectInstance(Shield.INSTANCE,owner.hp/5,2,owner));
	}


	@Override
	public String getDisplayText(SkillInstance instance) {
		return "攻击前，获得生命值20%护盾。";
	}

	@Override
	public String getStatusText(SkillInstance instance) {
		return "坚盾";
	}

}
