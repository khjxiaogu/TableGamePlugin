package com.khjxiaogu.TableGames.game.chess.skills;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Skill;
import com.khjxiaogu.TableGames.game.chess.SkillInstance;

public class HealerSkill implements Skill{

	public HealerSkill() {
	}

	@Override
	public void beforeAttack(SkillInstance instance, Combatant owner, Combatant character) {
		Skill.super.beforeAttack(instance, owner, character);
		instance.usedTime++;
		int maxHealth=Integer.MAX_VALUE;
		Combatant toheal=null;
		for(Combatant comba:owner.party.list) {
			if(comba.hp>0&&comba.hp<maxHealth) {
				toheal=comba;
				maxHealth=comba.hp;
			}
		};
		if(toheal!=null)
			toheal.hp+=4;
	}


	@Override
	public String getDisplayText(SkillInstance instance) {
		return "攻击前，为所在队伍生命值最低的角色恢复4生命。";
	}

	@Override
	public String getStatusText(SkillInstance instance) {
		return "疗愈";
	}

}
