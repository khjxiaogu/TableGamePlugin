package com.khjxiaogu.TableGames.game.chess;

public interface Effect {
	default void gameStart(EffectInstance instance,Combatant owner,Party opponent) {};
	default void battleStart(EffectInstance instance,Combatant owner,Party opponent) {};

	default void rowStart(EffectInstance instance,Combatant owner,Combatant opponent) {};
	default void beforeAttack(EffectInstance instance,Combatant owner,Combatant opponent) {};
	default float onAttack(EffectInstance instance,Combatant owner,Combatant target,float damage) {return damage;};
	default float onAttacked(EffectInstance instance,Combatant owner,Combatant attacker,float damage) {return damage;};
	default void afterAttack(EffectInstance instance,Combatant owner,Combatant opponent) {};

	default void rowEnd(EffectInstance instance,Combatant owner,Combatant opponent) {};
	default void battleEnd(EffectInstance instance,Combatant owner,Party opponent) {};
	boolean isStillValid(EffectInstance instance,Combatant owner);
	String getDisplayText(EffectInstance instance);
	String getStatusText(EffectInstance instance);
	String getDescription();
	default boolean isVisible() {
		return true;
	}
}
