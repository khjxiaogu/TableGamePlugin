package com.khjxiaogu.TableGames.game.chess.skills;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;

public class WarriorSkill implements Skill {

	public WarriorSkill() {
	}
	@Override
	public void afterAttack(SkillInstance instance, Combatant owner, Combatant character) {
		Skill.super.beforeAttack(instance, owner, character);
		instance.usedTime++;
		owner.attack++;
	}

	@Override
	public String getDisplayText(SkillInstance instance) {
		return "攻击后，获得1攻击。";
	}

	@Override
	public String getStatusText(SkillInstance instance) {
		return "怒气";
	}

}
