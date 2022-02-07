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
package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Demon extends Werewolf {

	public Demon(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return 1.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return 1.1;
	}

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你每晚除了杀人外可以查验一个人是否神职。";
	}

	@Override
	public void onTurnStart() {
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate("恶魔，你可以查验一个人是否神职。\n格式：“查验 游戏号码”\n如：“查验 1”");
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
					if (p instanceof Werewolf) {
						super.sendPrivate("选择的游戏号码是狼人，请重新输入");
						return;
					}
					game.logger.logSkill(this, p, "恶魔查验");
					EndTurn();
					super.releaseListener();
					super.sendPrivate(p.getMemberString() + "是" + (p.getRealFraction() == Fraction.God ? "神职"
							: (p.getRealFraction() == Fraction.Wolf) ? "狼人" : "平民"));
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“查验 游戏号码”！");
				}
			}
		});
	}

	@Override
	public void onDieSkill(DiedReason dir) {
	}

	@Override
	public boolean shouldWaitDeathSkill() {
		return super.shouldWaitDeathSkill();
	}

	@Override
	public boolean canDeathSkill(DiedReason dir) {
		return true;
	}

	@Override
	public void doDaySkillPending(String s) {
	}

	@Override
	public void addDaySkillListener() {
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "恶魔";
	}

}
