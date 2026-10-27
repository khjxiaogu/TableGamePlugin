package com.khjxiaogu.TableGames.game.chess;
public class SkillInstance {
    public final Skill type;
    public int cooldown;
    public int usedTime;
    
    public SkillInstance(Skill type) {
        this.type=type;
    }
    public void applyCooldown(int num) {
    	this.cooldown=num;
    }

    public void gameStart(Combatant owner, Party opponent) {
        type.gameStart(this, owner, opponent);
    }

    public void battleStart(Combatant owner, Party opponent) {
    	if(cooldown>0)
    		cooldown--;
        type.battleStart(this, owner, opponent);
    }

    public void beforeAttack(Combatant owner, Combatant character) {
        type.beforeAttack(this, owner, character);
    }

    public float onAttack(Combatant owner, Combatant character, float damage) {
        return type.onAttack(this, owner, character, damage);
    }

    public float onAttacked(Combatant owner, Combatant character, float damage) {
        return type.onAttacked(this, owner, character, damage);
    }

    public void afterAttack(Combatant owner, Combatant character) {
        type.afterAttack(this, owner, character);
    }
    public void rowStart(Combatant owner, Combatant character) {
        type.rowStart(this, owner, character);
    }
    public void rowEnd(Combatant owner, Combatant character) {
        type.rowEnd(this, owner, character);
    }

    public void battleEnd(Combatant owner, Party opponent) {
        type.battleEnd(this, owner, opponent);
    }
	public String getDisplayText() {
		return type.getDisplayText(this);
	}
	public String getStatusText() {
		return type.getStatusText(this);
	}
}