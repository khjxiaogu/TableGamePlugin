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

public class Crow extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你每天晚上都可以诅咒一个人，第二天驱逐投票中，他会额外被投一票，并且会显示诅咒情况。不能连续诅咒同一个人。";
	}

	public Crow(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		Villager last = game.lastCursed;
		sendPrivate(game.getAliveList());
		super.sendPrivate(
				"你可以诅咒一个人，让他在明天的投票之中被额外投一票。\n请私聊选择诅咒的人，你有60秒的考虑时间。\n格式：“诅咒 游戏号码”\n如果无需诅咒，则无需发送任何内容，等待时间结束即可。");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("诅咒")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("诅咒", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					if (p == last) {
						super.sendPrivate("选择的游戏号码上次已经被诅咒，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					increaseSkilledAccuracy(p.onVotedAccuracy());
					game.logger.logSkill(this, p, "诅咒");
					game.cursed = p;
					super.sendPrivate(p.getMemberString() + "获得了诅咒！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“诅咒 游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return 0.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.3;
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
		return "乌鸦";
	}
}
