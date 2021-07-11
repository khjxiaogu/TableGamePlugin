package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.Werewolf;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class WereWolfBot extends GenericBot {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2565299911384064789L;

	public WereWolfBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPrivate(String msg) {
		if (msg.startsWith("请私聊投票")) {
			List<Villager> canvote = game.getCanVote();
			if (canvote == null) {
				canvote = new ArrayList<>(game.playerlist);
			} else {
				canvote = new ArrayList<>(canvote);
			}
			canvote.removeIf(v -> v.isDead() || v instanceof Werewolf);
			this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
			return;
		} else if (msg.startsWith("请私聊选择要杀的人")) {
			for (Villager v : game.playerlist) {
				if (v instanceof Werewolf && !v.isDead() && v.getId() > 10000) {
					this.sendAsBot("放弃");
					super.onPrivate(msg);
					return;
				}
			}
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (!(v instanceof Werewolf) && !v.isDead()) {
					vx.add(v);
				}
			}
			this.sendAsBot("投票" + vx.get(rnd.nextInt(vx.size())).getId());
			return;
		}
		super.onPrivate(msg);
	}
}
