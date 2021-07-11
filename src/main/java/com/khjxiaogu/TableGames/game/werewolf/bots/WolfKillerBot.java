package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.TableGames.game.werewolf.Fraction;
import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class WolfKillerBot extends GenericBot {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6435365143630077164L;

	public WolfKillerBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("狩猎一个人")) {
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (v.getRealFraction() != Fraction.God) {
					vx.add(v);
				}
			}
			int l = game.playerlist.indexOf(vx.get(rnd.nextInt(vx.size())));
			this.sendAsBot("猎杀" + l);
		}
		super.onPrivate(msg);
	}

}
