package com.khjxiaogu.TableGames.werewolf;

import java.util.ArrayList;
import java.util.List;

public class WolfKillerBot extends GenericBot {

	public WolfKillerBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("狩猎一个人")) {
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (v.getFraction() != Fraction.God) {
					vx.add(v);
				}
			}
			int l = game.playerlist.indexOf(vx.get(rnd.nextInt(vx.size())));
			this.sendAsBot("猎杀" + l);
		}
		super.onPrivate(msg);
	}

}
