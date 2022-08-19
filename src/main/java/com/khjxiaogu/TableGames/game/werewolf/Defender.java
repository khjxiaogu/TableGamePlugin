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
package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Defender extends Villager {
	public Defender(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 0.25;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.3;
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你每晚可以选择保护一个人包括自己免于死亡，但是不能连续两晚保护同一个人。如果被保护的人同时被女巫救，则对方依然死亡。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		sendPrivate(game.getAliveList(this));
		super.sendPrivate(
				"守卫，你可以保护一个人包括自己免于死亡，不能连续两次保护同一个人，请私聊选择保护的人，你有一分钟的考虑时间\n格式：“保护 游戏号码”\n如：“保护 1”\n如果放弃保护，则无需发送任何内容，等待时间结束即可。");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("保护")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("保护", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					if (p.lastIsGuarded) {
						super.sendPrivate("选择的游戏号码上次已经被保护，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					game.logger.logSkill(this, p, "保护");
					p.isGuarded = true;
					super.sendPrivate(p.getMemberString(this) + "获得了保护！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“保护 游戏号码”！");
				}
			}
		});
	}

	@Override
	public void fireSkill(Villager p, int skid) {
		p.isGuarded = true;

	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public String getRole() {
		return "守卫";
	}

}
