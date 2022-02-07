/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.spwarframe.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.TableGames.game.spwarframe.FakePlayer;
import com.khjxiaogu.TableGames.game.spwarframe.GameManager;
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.Fraction;
import com.khjxiaogu.TableGames.game.spwarframe.GameManager.GameTurn;
import com.khjxiaogu.TableGames.game.spwarframe.Player;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.CantSaveException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidInterruptedTargetException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.InvalidSkillParamException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.RoleDiedException;
import com.khjxiaogu.TableGames.game.spwarframe.Exceptions.SkillException;
import com.khjxiaogu.TableGames.game.spwarframe.events.DeadAnnounceEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.Event;
import com.khjxiaogu.TableGames.game.spwarframe.events.FractionRevalEvent;
import com.khjxiaogu.TableGames.game.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.game.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.game.spwarframe.skill.SkillType;

public abstract class Role {
	public boolean isBoss() {
		return isBoss;
	}

	public void setFraction(Fraction fraction) {
		this.fraction = fraction;
	}

	public void setNumber(int num) {
		owner.setNumber(num);
	}

	public void removeNumber() {
		owner.removeNumber();
	}

	public void listenMessage(Consumer<List<String>> msgc) {
		owner.listenMessage(msgc);
	}

	public void makeSpeak() {
		owner.makeSpeak();
	}

	public void makeMute() {
		owner.makeMute();
	}

	public static class RevalSkill extends Skill {
		public RevalSkill(GameManager game, Role owner) {
			super(game, owner);
		}


		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}

		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			return super.fireEvent(new FractionRevalEvent(owner, target, this).setMainEvent(true));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public String getName() {
			return "探查";
		}

		@Override
		public boolean isUsableOn(GameTurn turn) {
			return turn == GameTurn.DAY || super.isUsableOn(turn);
		}

		@Override
		public String getDesc() {
			return "作为公开人物拥有的探查技能，可以探查一个人的阵营";
		}

		@Override
		public int getMaxRemain() {
			return 1;
		}

	}

	public static class KillSkill extends Skill {

		public KillSkill(GameManager game, Role owner) {
			super(game, owner);
		}
		@Override
		public String[] getSpecialParamType() {
			return new String[] { "游戏号码" };
		}
		@Override
		protected boolean onSkillUse(List<String> params) throws SkillException {
			if (params.size() < 1)
				throw new InvalidSkillParamException(this);
			Role target = game.getSkillPlayerByName(params.get(0));
			target.ensureAlive();
			return super.fireEvent(new KillEvent(owner, target, this).setMainEvent(true));
		}

		@Override
		public SkillType getType() {
			return SkillType.SPECIAL;
		}

		@Override
		public String getName() {
			return "境主";
		}

		@Override
		public String getDesc() {
			return "作为境主拥有的杀人技能";
		}

		@Override
		public int getMaxRemain() {
			return 999;
		}

	}

	Fraction fraction;
	Fraction fakefraction;
	public final GameManager room;
	public List<KillEvent> lastkill;
	public boolean alive = true;
	public boolean exposed = false;
	public boolean fexposed = false;
	int numKills = 0;
	boolean isLeader = false;
	boolean isBoss = false;
	Player owner;
	Fraction belong;
	List<Skill> skills = new ArrayList<>();
	List<Skill> sks = new ArrayList<>(2);
	List<Role> binded = new ArrayList<>(1);

	public Role(GameManager room) {
		this.room = room;
		owner=new FakePlayer(room);
	}

	public Role bind(Player p) {
		owner = p;
		return this;
	}

	public abstract String getName();

	public abstract boolean isMale();

	public void removeSkill(Class<? extends Skill> skc) {
		skills.removeIf(sk -> skc.isInstance(sk));
	}

	@SuppressWarnings("unchecked")
	public <T extends Skill> T getSkill(Class<T> skc) {
		for (Skill sk : skills) {
			if (skc.isInstance(sk))
				return (T) sk;
		}
		return null;
	}

	public Collection<Skill> getAllSkills() {
		return skills;
	}

	public void addSkill(Skill sk) {
		skills.add(sk);
	}

	public void ensureAlive() throws SkillException {
		if (isAlive())
			return;
		throw new RoleDiedException(this);
	}

	public <T extends Event> void listen(Class<T> evc, Consumer<T> listener) {
		room.RegisterListener(this, evc, listener);
	}

	public void listen(Consumer<Event> listener) {
		room.RegisterListener(this, listener);
	}

	public boolean isAlive() {
		return alive;
	}

	public void bind(Role caller) {
		internalBind(caller);
		caller.internalBind(this);
	}

	private void internalBind(Role target) {
		binded.add(target);
	}

	public void setLeader() {
		if(isLeader)return;
		isLeader = true;
		expose();
		new RevalSkill(room, this);
	}
	public void setBoss() {
		if(isBoss)return;
		isBoss=true;
		new KillSkill(room,this);
	}
	Role br;
	public void setBossNormal(Role bossRole) {
		isBoss=true;
		br=bossRole;
	}
	public void onTurnEnd() {
		skills.removeIf(s -> !s.onTurnEnd());
	}

	public void onBeforeTurnEnd() {
		skills.removeIf(s -> !s.onBeforeTurnEnd());
	}

	public void onTurnStart() {
		skills.removeIf(s -> !s.onTurnStart());
	}

	public Fraction getRevalFraction() {
		return fraction;
	}

	public String getRevalPlayer() {
		return getName();
	}

	public String getPlayer() {
		return owner.getName();
	}

	public void sendMessage(String msg) {
		owner.sendMessage(msg);
	}

	public boolean isFractionExposed() {
		return fexposed;
	}

	public void exposeFraction() {
		fexposed = true;
	}

	public void expose() {
		fexposed = true;
		exposed = true;
	}

	public int getKillCount() {
		return numKills;
	}

	public void askDaySkill() {
		sks.clear();
		for (Skill s : skills) {
			if (s.isUsableOn(GameTurn.DAY) && s.available()) {
				sks.add(s);
			}
		}
		if (sks.size() == 0)
			return;
		askSkills(sks, false);
	}

	public boolean askAttackSkill() {
		sks.clear();
		for (Skill s : skills) {
			if (s.isUsableOn(GameTurn.ATTACK) && s.available()) {
				sks.add(s);
			}
		}
		if (sks.size() == 0)
			return false;
		askSkills(sks, true);
		return true;
	}

	public boolean askSpecialSkill() {
		sks.clear();
		for (Skill s : skills) {
			if (s.isUsableOn(GameTurn.SPECIAL) && s.available()) {
				sks.add(s);
			}
		}
		if (sks.size() == 0)
			return false;
		askSkills(sks, true);
		return true;
	}

	List<Skill> tointerrupt;

	public boolean askInterruptSkill(List<Skill> skills) {
		tointerrupt = skills;
		sks.clear();
		for (Skill s : skills) {
			if (s.isUsableOn(GameTurn.INTERRUPT) && s.available()) {
				sks.add(s);
			}
		}
		if (sks.size() == 0)
			return false;
		StringBuilder avail = new StringBuilder("场上技能：");
		int i = 0;
		for (Skill s : skills) {
			avail.append("\n").append(i++).append("、").append(s.getName());
		}
		sendMessage(avail.toString());
		askSkills(sks, true);
		return true;
	}

	Collection<Role> todie;

	public boolean askSaveSkill(Collection<Role> todie) {
		this.todie = todie;
		sks.clear();
		for (Skill s : skills) {
			if (s.isUsableOn(GameTurn.SAVE) && s.available()) {
				sks.add(s);
			}
		}
		if (sks.size() == 0)
			return false;
		StringBuilder avail = new StringBuilder("死亡角色：");
		for (Role s : todie) {
			avail.append("\n").append(s.getName());
		}
		sendMessage(avail.toString());
		askSkills(sks, true);
		return true;
	}

	public void askSkills(Collection<Skill> sks, boolean canSkip) {
		StringBuilder sb = new StringBuilder("当前可用技能：\n");
		for (Skill s : sks) {
			sb.append(s.getName()).append("：请输入“").append(s.getName());
			String[] params = s.getSpecialParamType();
			if (params != null) {
				for (String sx : params) {
					sb.append(" <").append(sx).append(">");
				}
			}
			sb.append("”来使用\n");
		}
		if (canSkip) {
			sb.append("如果都不需要使用，请输入“跳过”来跳过。");
		}
		owner.sendMessage(sb.toString());
		listenSkills(sks, canSkip);
	}

	public void listenSkills(Collection<Skill> sk, boolean canSkip) {
		owner.listenMessage(s -> {
			if (canSkip && s.contains("跳过")) {
				onSkillUsed();
				return;
			}
			for (Skill ss : sk) {
				if (ss.getName().equals(s.get(0))) {
					if (ss.available())
						if (!ss.onSkillCall(s.subList(1, s.size()))) {
							owner.sendMessage("您仍可继续使用技能。");
						}else if(canSkip) {
							onSkillUsed();
						}
					break;
				}
			}
		});
	}

	public void onSkillUsed() {

		room.skipSkillWait();
	}

	public Skill getInterruptSkillByNum(String num) throws SkillException {
		int n = Integer.parseInt(num);
		if (n > 0 && tointerrupt.size() > n)
			return tointerrupt.get(n);
		throw new InvalidInterruptedTargetException(num);

	}

	public void checkCanSave(Role save) throws SkillException {
		if (!todie.contains(save))
			throw new CantSaveException(save);

	}

	public Fraction getFraction() {
		return fraction;
	}
	public void kill() {
		alive = false;
		for (Role role : binded) {
			if(role.alive) {
				role.kill();
				DeadAnnounceEvent dae = new DeadAnnounceEvent(role);
				dae.setCounter(lastkill.size());
				role.lastkill = lastkill;
				room.fireEvent(dae);
			}
		}
		if(isBoss&&br!=null) {
			owner.sendMessage("你是境主，你死了，请选择是否现身。如果要，发送“是”，否则发送“否”");
			owner.listenMessage(s -> {
				if (s.contains("是")) {
					room.doBossShowUp();
				}else if(s.contains("否")) {
					br.kill();
				}
				room.skipSkillWait();
			});
			room.waitForSkill(6000);
			owner.removeListener();
		}
	}
	public void removeListener() {
		owner.removeListener();
	}

	public void reborn() {
		alive = true;
		for (Role role : binded) {
			role.reborn();
		}
	}

	public void addKillCount() {
		numKills++;
	}

	public Role getBr() {
		return br;
	}

	public boolean isLeader() {
		return isLeader;
	}
}
