package com.khjxiaogu.TableGames.game.chess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PlayerSave {
	public List<CharacterInstance> battle=new ArrayList<>(5);
	public List<CharacterInstance> backup=new ArrayList<>(7);
	public int wins;
	public int health;
	public int money;
	public PlayerSave() {
	}
	public Party createParty() {
		Party p=new Party();
		for(CharacterInstance ci:battle) {
			p.list.add(ci.createCharacter(p));
		}
		return p;
	}
	public boolean hasStillSeat() {
		return battle.size()<5||backup.size()<7;
	}
	public void addActor(Characters chara) {

		CharacterInstance ci=new CharacterInstance(chara,1);
		if(battle.size()<5) {
			battle.add(ci);
		}else
			backup.add(ci);
	}
	public void regroup(int[] assign) {
		List<CharacterInstance> out=new ArrayList<>();

		HashSet<CharacterInstance> indexSet = new HashSet<>(assign.length);
		out.addAll(battle);
		out.addAll(backup);
		battle.clear();
		backup.clear();
		for(int i:assign) {
			CharacterInstance contained=out.get(i-1);
			battle.add(contained);
			indexSet.add(contained);
		}
		
		for(CharacterInstance i:out) {
			if(!indexSet.contains(i))
				backup.add(i);
		}
		
	}
	public String getDesc() {
		StringBuilder sb=new StringBuilder();
		int j=0;
		Party party=new Party();

		sb.append("编队：");
		for(CharacterInstance ci:battle) {
			sb.append("\n");
			sb.append(++j).append(ci.createCharacter(party).getDesc());
		}
		sb.append("\n待命：");
		for(CharacterInstance ci:backup) {
			sb.append("\n");
			sb.append(++j).append(ci.createCharacter(party).getDesc());
		}
		sb.append("金币：").append(money);

		sb.append("生命：").append(health);
		sb.append("胜局：").append(health);
		return sb.toString();
	};
}
