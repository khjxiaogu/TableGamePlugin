/**
 * Mirai Song Plugin
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
package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Muter extends Villager {

	public Muter(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，第二天晚上开始可以禁言一个人，第二天的正常发言回合，他不能发言，不能连续两晚禁言同一个人。";
	}

	@Override
	public void onTurn() {
		if (game.isFirstNight())
			return;
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate(
				"你可以禁言一个人，让他在明天的发言回合不能发言。\n请私聊选择禁言的人，你有60秒的考虑时间。\n格式：“禁言 游戏号码”\n如果无需禁言，则无需发送任何内容，等待时间结束即可。");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("禁言")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("禁言", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					if (p.lastIsMuted) {
						super.sendPrivate("选择的游戏号码上次已经被禁言，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					// increaseSkilledAccuracy(p.onVotedAccuracy());
					game.logger.logSkill(this, p, "禁言");
					p.isMuted = true;
					super.sendPrivate(p.getMemberString() + "获得了禁言！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“禁言 游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return 0.35;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.25;
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
		return "禁言长老";
	}
}
