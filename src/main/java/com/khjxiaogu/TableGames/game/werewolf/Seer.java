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

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Seer extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Seer(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你每晚可以查验一个人的身份牌。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate("预言家，你可以查验一个人，请私聊选择查验的人，你有一分钟的考虑时间\n格式：“查验 游戏号码”\n如：“查验 1”");
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
					EndTurn();
					super.releaseListener();
					game.logger.logSkill(this, p, "查验");
					super.sendPrivate(p.getMemberString() + "是" + p.getPredictorRole());
					if (p instanceof NightmareKnight) {
						NightmareKnight nk = (NightmareKnight) p;
						if (!nk.isSkillUsed) {
							nk.isSkillUsed = true;
							game.kill(this, DiedReason.Reflect);
						}
					}
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“查验 游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return 0.4;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.4;
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
		return "预言家";
	}
}
