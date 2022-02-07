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
package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.khjxiaogu.TableGames.game.werewolf.Fraction;
import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.BotUser;

public class GenericBot extends BotUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6801990493391227114L;
	WerewolfGame game;
	static Random rnx = new SecureRandom();
	public static final String talkKey = "五分钟时间进行陈述";
	public static final String deadKey = "说出你的遗言";
	Random rnd = new Random(GenericBot.rnx.nextLong());

	@Override
	public void onPublic(String msg) {
		super.onPublic(msg);
		if (msg.contains(GenericBot.talkKey)) {
			sendAtAsBot("好人牌，过。");
		} else if (msg.contains("说出你的遗言")) {
			if (((Villager) super.getPlayer().getRoleObject()).getRealFraction() != Fraction.Wolf) {
				sendBotMessage("我是" + ((Villager) super.getPlayer().getRoleObject()).getRole());
			} else {
				sendBotMessage("我是平民");
			}
			sendAtAsBot(" 过");
		}
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.startsWith("请私聊投票")) {
			List<Villager> canvote = game.getCanVote();
			if (canvote == null) {
				canvote = new ArrayList<>(game.playerlist);
			} else {
				canvote = new ArrayList<>(canvote);
			}
			canvote.removeIf(v -> v.isDead());
			canvote.removeIf(v -> v ==super.getPlayer().getRoleObject());

			boolean isWolf = rnd.nextInt(3)<3;
			if (isWolf) {
				canvote.removeIf(v -> !v.getRealFraction().equals(Fraction.Wolf));
				if (canvote.size() == 0) {
					this.sendAsBot("弃权");
				}
				this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
			} else {
				canvote.removeIf(v -> v.getRealFraction().equals(Fraction.Wolf));
				if (canvote.size() == 0) {
					this.sendAsBot("弃权");
					return;
				}
				this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
			}
		}
		super.onPrivate(msg);
	}

	public GenericBot(AbstractBotUser p, WerewolfGame gam) {
		super(p);
		game = gam;
	}
	public void sendAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			getPlayer().sendAsBot(msg);
		}, 5000);
	}
	public void sendAtAsBot(String msg) {
		game.getScheduler().executeLater(() -> {
			getPlayer().sendAtAsBot("@" +game.getGroup().getHostNameCard() + msg);
		}, 6000);
	}
	public void sendBotMessage(String msg) {
		try {
			Thread.sleep(800);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getPlayer().sendBotMessage(msg);

	}

}
