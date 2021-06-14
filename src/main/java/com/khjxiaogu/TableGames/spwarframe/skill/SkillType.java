package com.khjxiaogu.TableGames.spwarframe.skill;

import com.khjxiaogu.TableGames.spwarframe.GameManager.GameTurn;

public enum SkillType {
	ATTACK(true,false,false,false),
	SPECIAL(false,true,false,false),
	INTERRUPT(false,false,true,false),
	SAVE(false,false,false,true),
	PASSIVE(false,false,false,false);
	public boolean isAttack() { return attack; }
	public boolean isSpecial() { return special; }
	public boolean isInterrupt() { return interrupt; }
	public boolean isSave() { return save; }
	private boolean attack;
	private boolean special;
	private boolean interrupt;
	private boolean save;
	private SkillType(boolean attack, boolean special, boolean interrupt, boolean save) {
		this.attack = attack;
		this.special = special;
		this.interrupt = interrupt;
		this.save = save;
	}
	public boolean isUsableOn(GameTurn turn) {
		return turn.isAvailableFor(this);
	}
}
