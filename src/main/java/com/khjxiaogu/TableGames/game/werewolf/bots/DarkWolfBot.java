package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.Werewolf;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class DarkWolfBot extends WereWolfBot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5232651679019906500L;

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("你可以选择打死另一个人")) {
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (!(v instanceof Werewolf) && !v.isDead()) {
					vx.add(v);
				}
			}
			this.sendAsBot("杀死" + vx.get(rnd.nextInt(vx.size())).getId());
			return;
		}
		super.onPrivate(msg);
	}

	public DarkWolfBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

}
