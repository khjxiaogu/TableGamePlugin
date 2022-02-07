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

public class HiddenWolf extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 1;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.9;
	}

	public HiddenWolf(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，其他狼人不知道你的存在，被查验永远返回平民，当其他狼人死亡后你可以杀人。";
	}

	@Override
	public void onGameStart() {
		super.sendPrivate("您的身份是：" + getRole());
		super.sendPrivate(getJobDescription());
		StringBuilder sb = new StringBuilder("其他狼人身份是：\n");
		for (Villager w : game.playerlist) {
			if (w instanceof Werewolf)
				if (!w.equals(this)) {
					sb.append(w.getMemberString() + "\n");
				}
		}
		super.sendPrivate(sb.toString());

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
		return 1;
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.Wolf;
	}

	@Override
	public String getRole() {
		return "隐狼";
	}

	@Override
	public String getPredictorRole() {
		return "平民";
	}
}
