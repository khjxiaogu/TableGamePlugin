package com.khjxiaogu.TableGames.game.chess.effects;

import com.khjxiaogu.TableGames.game.chess.Combatant;
import com.khjxiaogu.TableGames.game.chess.Effect;
import com.khjxiaogu.TableGames.game.chess.EffectInstance;
import com.khjxiaogu.TableGames.game.chess.Party;

public class Strength implements Effect {
	public static final Strength INSTANCE=new Strength();
	public Strength() {
	}

	@Override
	public void battleEnd(EffectInstance instance, Combatant owner, Party opponent) {
		if(instance.level>0)
			instance.level--;
		Effect.super.battleEnd(instance, owner, opponent);
	}

	@Override
	public boolean isStillValid(EffectInstance instance, Combatant owner) {
		return instance.level>0;
	}

	@Override
	public float onAttack(EffectInstance instance, Combatant owner, Combatant target, float damage) {
		if(damage<=0)
			return damage;
		return Effect.super.onAttack(instance, owner, target, damage)+instance.level;
	}

	@Override
	public String getDisplayText(EffectInstance instance) {
		return "力量"+instance.level;
	}

	@Override
	public String getStatusText(EffectInstance instance) {
		return "力"+instance.level;
	}

	@Override
	public String getDescription() {
		return "按等级值增加伤害，回合结束时等级减少1";
	}

}
