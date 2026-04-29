package com.khjxiaogu.TableGames.game.chess;
public interface Skill {
	/** 游戏开始事件*/
    default void gameStart(SkillInstance instance, Combatant owner,Party opponent) {};
	/** 轮次开始事件*/
    default void battleStart(SkillInstance instance, Combatant owner,Party opponent) {};
	/** 回合开始事件*/
	default void rowStart(SkillInstance instance,Combatant owner,Combatant opponent) {};
	/** 攻击前事件*/
    default void beforeAttack(SkillInstance instance, Combatant owner, Combatant opponent) {};
	/** 进行攻击事件*/
    default float onAttack(SkillInstance instance, Combatant owner, Combatant target, float damage) {return damage;};
	/** 受击事件*/
    default float onAttacked(SkillInstance instance, Combatant owner, Combatant attacker, float damage) {return damage;};
    /** 攻击后事件*/
    default void afterAttack(SkillInstance instance, Combatant owner, Combatant opponent) {};
    /** 回合结束事件*/
	default void rowEnd(SkillInstance instance,Combatant owner,Combatant opponent) {};
	/** 轮次结束事件*/
    default void battleEnd(SkillInstance instance, Combatant owner,Party opponent) {};
	String getDisplayText(SkillInstance instance);
	String getStatusText(SkillInstance instance);
}