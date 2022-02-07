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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.werewolf.bots;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.TableGames.game.werewolf.Fraction;
import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;

public class WitchBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 741149304485873268L;
	boolean selfrescue = false;
	boolean usedrescue = false;
	boolean exposed = false;
	int lastrescue = -1;
	int lastpoisoned = -1;

	public WitchBot(AbstractBotUser p,WerewolfGame gam){super(p,gam);}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("今晚死亡情况是：") && !msg.contains("今晚没有人死亡")) {
			if (game.getTokill().contains(getPlayer().getRoleObject())) {
				if (game.isFirstNight()) {
					save(game.playerlist.indexOf(getPlayer().getRoleObject()));
					selfrescue = true;
				} else {
					decidePoison();
				}
			} else {
				save(game.playerlist.indexOf(game.getTokill().iterator().next()));
			}
			usedrescue = true;
			return;
		}
		if (msg.contains("女巫，你有")) {
			if (exposed) {
				decidePoison();
			}
			return;
		}
		super.onPrivate(msg);
	}

	@Override
	public void onPublic(String msg) {
		if (!exposed && game.getAliveCount() <= 6) {
			exposed = true;
		}
		if (msg.contains(GenericBot.talkKey)) {
			if (usedrescue) {
				usedrescue = false;
				exposed = true;
				if (!selfrescue) {
					sendBotMessage("女巫，" + lastrescue + "号银水，今晚随机毒一个。");
				} else {
					sendBotMessage("女巫，昨晚自救，今晚随机毒一个。");
				}
				sendAtAsBot(" 过");
				return;
			}
			if (exposed) {
				if (lastpoisoned != -1) {
					sendBotMessage("女巫，昨晚毒了" + lastpoisoned + "号");
				} else {
					sendBotMessage("女巫牌");
				}
				sendAtAsBot(" 过");
				return;
			}
		}
		super.onPublic(msg);
	}

	private void save(int num) {
		lastrescue = num;
		this.sendAsBot("救" + num);
	}

	private void decidePoison() {
		List<Villager> vx = new ArrayList<>();
		for (Villager v : game.playerlist) {
			if (v.getRealFraction() != Fraction.God) {
				vx.add(v);
			}
		}
		int l = game.playerlist.indexOf(vx.get(rnd.nextInt(vx.size())));
		lastpoisoned = l;
		this.sendAsBot("毒" + l);
	}
}
