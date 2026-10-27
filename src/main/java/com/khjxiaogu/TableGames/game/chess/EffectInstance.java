package com.khjxiaogu.TableGames.game.chess;

public class EffectInstance implements Comparable<EffectInstance>{
	public final Effect type;
	public int level;
	public int alive;
	public final Combatant source;
	public int priority;
	public int addOrder;
	public EffectInstance(Effect type, int level, int alive, Combatant source) {
		super();
		this.type = type;
		this.level = level;
		this.alive = alive;
		this.source = source;
	}
	public EffectInstance(Effect type, int level, Combatant source) {
		this(type,1,-1,source);
	}
	public EffectInstance(Effect type, Combatant source) {
		this(type,1,source);
	}
	public void gameStart(Combatant owner,Party opponent) {
		type.gameStart(this, owner,opponent);
	}

	public void battleStart(Combatant owner,Party opponent) {
		if(alive>0)
			alive--;
		type.battleStart(this, owner, opponent);
	}

	public void beforeAttack(Combatant owner, Combatant opponent) {
		type.beforeAttack(this, owner, opponent);
	}

	public float onAttack(Combatant owner, Combatant target, float damage) {
		return type.onAttack(this, owner, target, damage);
	}

	public float onAttacked(Combatant owner, Combatant attacker, float damage) {
		return type.onAttacked(this, owner, attacker, damage);
	}

	public void afterAttack(Combatant owner, Combatant opponent) {
		type.afterAttack(this, owner, opponent);
	}

	public void battleEnd(Combatant owner, Party opponent) {
		type.battleEnd(this, owner, opponent);
	}
    public void rowStart(Combatant owner, Combatant character) {
        type.rowStart(this, owner, character);
    }
    public void rowEnd(Combatant owner, Combatant character) {
        type.rowEnd(this, owner, character);
    }
	public boolean isStillValid(Combatant owner) {
		return alive!=0&&type.isStillValid(this, owner);
	}
	public String getDisplayText() {
		return type.getDisplayText(this);
	}
	public String getStatusText() {
		return type.getStatusText(this);
	}
	@Override
	public int compareTo(EffectInstance o) {
		int comp=o.priority-this.priority;
		if(comp>0)
			return 1;
		if(comp<0)
			return -1;
		if(this.alive!=o.alive) {
			if(this.alive==-1)
				return 1;
			if(o.alive==-1)
				return -1;
			comp=this.alive-o.alive;
			if(comp>0)
				return 1;
			if(comp<0)
				return -1;
		}
		comp=this.addOrder-o.addOrder;
		if(comp>0)
			return 1;
		if(comp<0)
			return -1;
		return 0;
	}
}
