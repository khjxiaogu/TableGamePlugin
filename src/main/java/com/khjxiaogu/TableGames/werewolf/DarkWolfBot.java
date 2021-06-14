package com.khjxiaogu.TableGames.werewolf;

import java.util.ArrayList;
import java.util.List;

public class DarkWolfBot extends WereWolfBot {

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("你可以选择打死另一个人")) {
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (!(v instanceof Werewolf) && !v.isDead) {
					vx.add(v);
				}
			}
			this.sendAsBot("杀死" + vx.get(rnd.nextInt(vx.size())).getId());
			return;
		}
		super.onPrivate(msg);
	}

	public DarkWolfBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

}
