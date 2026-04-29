package com.khjxiaogu.TableGames.game.chess;

import java.util.ArrayList;

public class Party {
	public ArrayList<Combatant> list=new ArrayList<>();
	public ArrayList<Combatant> actionOrder=new ArrayList<>();
	public Party() {
	}
	public boolean isRefilled;
	public Combatant popNext() {
		for(int i=0;i<2;i++) {
			if(actionOrder.isEmpty()) {
				isRefilled=true;
				for(Combatant cbt:list) {
					actionOrder.add(cbt);
				}
			}
			while(!actionOrder.isEmpty()) {
				Combatant comb= actionOrder.remove(0);
				if(comb.hp>0) {
					return comb;
				}
			}
		}
		return null;
	}
	public void gameStart(Party opponent) {
		int idx=0;
		for(Combatant cbt:list) {
			idx++;
			cbt.index=idx;
			cbt.gameStart(opponent);
		}
	}
	public String getDesc() {
		StringBuilder sb=new StringBuilder();
		sb.append(list.get(0).index).append(list.get(0).getDesc());
		if(list.size()>1)
			for(int i=1;i<list.size();i++)
				sb.append("\n").append(list.get(i).index).append(list.get(i).getDesc());
		return sb.toString();
	};

}
