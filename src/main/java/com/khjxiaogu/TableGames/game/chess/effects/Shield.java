package com.khjxiaogu.TableGames.game.chess.effects;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Effect;
import com.khjxiaogu.TableGames.game.chess.EffectInstance;

public class Shield implements Effect {
	public static final Effect INSTANCE=new Shield();
	private Shield() {
	}

	@Override
	public float onAttacked(EffectInstance instance, Combatant owner, Combatant attacker, float damage) {
		if(damage<=0)
			return damage;
		int lvl=instance.level;
		if(lvl>=damage) {
			double dmg=Math.ceil(damage);
			lvl-=dmg;
			damage-=dmg;
		}else {
			damage-=lvl;
			lvl=0;
		}
		instance.level=lvl;
		return Effect.super.onAttacked(instance, owner, attacker, damage);
	}

	@Override
	public String getDisplayText(EffectInstance instance) {
		return "护盾"+instance.level;
	}

	@Override
	public String getStatusText(EffectInstance instance) {
		return "防"+instance.level;
	}

	@Override
	public boolean isStillValid(EffectInstance instance, Combatant owner) {
		return instance.level>0;
	}

	@Override
	public String getDescription() {
		return "抵御等级值点伤害并削减等级";
	}

}
