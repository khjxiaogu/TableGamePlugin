package com.khjxiaogu.TableGames.game.chess.skills;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;

public class AssassinSkill implements Skill{

	public AssassinSkill() {
	}


	@Override
	public void afterAttack(SkillInstance instance, Combatant owner, Combatant opponent) {
		Skill.super.afterAttack(instance, owner, opponent);
		if(instance.cooldown<=0&&opponent.hp==0) {
			owner.party.actionOrder.add(0, owner);
			instance.applyCooldown(1);
		}
	}

	@Override
	public String getDisplayText(SkillInstance instance) {
		return "若攻击击杀对方，则获得一次行动机会（冷却1回合）";
	}

	@Override
	public String getStatusText(SkillInstance instance) {
		return "突袭"+(instance.cooldown>0?"(冷却中)":"");
	}

}
