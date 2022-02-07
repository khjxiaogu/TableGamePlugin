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

public class StatueDemon extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public StatueDemon(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你每晚可以查验一个人，不能重复查或者查自己。若场上所有其他狼人死亡，则你可以杀人。";
	}

	@Override
	public boolean canWolfTurn() {
		for (Villager inno : game.playerlist) {
			if (inno instanceof Werewolf && !inno.isDead())
				return false;
		}
		for (Villager inno : game.playerlist) {
			if (inno == this)
				return true;
			if (inno.getRealFraction() == Fraction.Wolf && !inno.isDead())
				return false;
		}
		return true;
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate("石像鬼，你可以查验一个人的身份，请私聊选择查验的人，你有30秒的考虑时间\n格式：“查验 游戏号码”\n如：“查验 1”");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("查验")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("查验", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					if (p == this) {
						super.sendPrivate("不能查验自己！");
						return;
					}
					if (p.isDemonChecked) {
						super.sendPrivate("此玩家已经被查验过！");
						return;
					}
					EndTurn();
					super.releaseListener();
					game.logger.logSkill(this, p, "石像鬼查验");
					p.isDemonChecked = true;
					super.sendPrivate(p.getMemberString() + "是" + p.getPredictorRole());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“查验 游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return 1;
	}

	@Override
	public double onSkilledAccuracy() {
		return 1;
	}

	@Override
	public void onWolfTurn() {
		for (Villager inno : game.playerlist) {
			if (inno instanceof Werewolf && !inno.isDead())
				return;
		}
		for (Villager inno : game.playerlist) {
			if (inno == this) {
				break;
			}
			if (inno.getRealFraction() == Fraction.Wolf && !inno.isDead())
				return;
		}
		StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate(game.getWolfSentence());
		game.vu.addToVote(this);
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("投票")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("投票", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					game.WolfVote(this, p);
					game.logger.logSkill(this, p, "狼人投票");
					super.sendPrivate("已投票给 " + p.getMemberString());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 游戏号码”！");
				}
			} else if (content.startsWith("放弃")) {
				EndTurn();
				super.releaseListener();
				game.NoVote(this);
				super.sendPrivate("已放弃");
			}
		});
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.Wolf;
	}

	@Override
	public String getRole() {
		return "石像鬼";
	}

	@Override
	public String getPredictorRole() {
		return "狼人";
	}

}
