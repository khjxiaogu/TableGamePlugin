package com.khjxiaogu.TableGames.game.chess;

import java.util.ArrayList;

public class Combatant {
	public Characters character;
	public Party party;
	public int attack;
	public int hp;
	public int index;
	ArrayList<SkillInstance> skills=new ArrayList<>();
	ArrayList<EffectInstance> effects=new ArrayList<>();
	public Combatant(Characters character,Party party,int atk,int hp,SkillInstance... skills) {
		this.character=character;
		this.party=party;
		this.attack=atk;
		this.hp=hp;
	
		for(SkillInstance skill:skills)
			this.skills.add(skill);
	}
	public int addOrder=0;
	public String getName() {
		return character.name();
	}
	public String getDesc() {
		StringBuilder sb=new StringBuilder(character.name());
		sb.append(" ATK:").append(attack).append(" HP:").append(hp);
		for(EffectInstance ei:effects) {
			sb.append(" ").append(ei.getStatusText());
		}
		sb.append(" 技能：");
		for(SkillInstance si:skills) {
			sb.append(si.getStatusText());
		}
		return sb.toString();
	}
	public void addEffectLevel(EffectInstance effect) {
		effect.addOrder=addOrder;
		addOrder++;
		effects.add(effect);
	}
	public void addEffectLevel(Combatant source,Effect effect,int level) {
		for(EffectInstance eff:effects) {
			if(eff.type==effect&&eff.source==source) {
				eff.level+=level;
				return;
			}
		}
		effects.add(new EffectInstance(effect,level,source));
	}
    public void gameStart(Party opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.gameStart(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.gameStart(this, opponent);
        }
    }

    public void battleStart(Party opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.battleStart(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.battleStart(this, opponent);
        }
    }
    public void rowStart(Combatant opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.rowStart(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.rowStart(this, opponent);
        }
    }
    public void beforeAttack(Combatant opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.beforeAttack(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.beforeAttack(this, opponent);
        }
    }

    public float onAttack(Combatant target, float damage) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            damage = ei.onAttack(this, target, damage);
        }
        for (SkillInstance si : skills) {
            damage = si.onAttack(this, target, damage);
        }
        return damage;
    }
    public float createAttack(Combatant target) {
    	return onAttack(target,attack);
    }
    public float onAttacked(Combatant attacker, float damage) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            damage = ei.onAttacked(this, attacker, damage);
        }
        for (SkillInstance si : skills) {
            damage = si.onAttacked(this, attacker, damage);
        }
        return damage;
    }
    public void receiveAttack(Combatant attacker,float damage) {
    	float val= onAttacked(attacker,damage);
    	if(val>=1) {
    		hp-=val;
    	}
    }

    public void afterAttack(Combatant opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.afterAttack(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.afterAttack(this, opponent);
        }
    }
    public void rowEnd(Combatant opponent) {
    	if(hp<0)hp=0;
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.rowEnd(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.rowEnd(this, opponent);
        }
        effects.removeIf(t->!t.isStillValid(this));
    }
    public void battleEnd(Party opponent) {
    	effects.sort(null);
        for (EffectInstance ei : effects) {
            ei.battleEnd(this, opponent);
        }
        for (SkillInstance si : skills) {
            si.battleEnd(this, opponent);
        }
    }
}
