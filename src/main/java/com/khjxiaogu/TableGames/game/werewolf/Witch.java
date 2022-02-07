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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Witch extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Witch(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	boolean hasPoison = true;
	boolean hasHeal = true;

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你有一瓶救人的解药和杀人的毒药，若你有解药，则你可以知道当晚死者，你每晚可以选用其中一瓶，每瓶药只能用一次。但是如果被解药救的人被守护，则此人依然会死。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		if (!hasHeal && !hasPoison) {
			super.sendPrivate("女巫，你没有药了。");
			return;
		}
		sendPrivate(game.getAliveList());
		StringBuilder sb = new StringBuilder("女巫，你有");
		if (hasPoison) {
			sb.append("一瓶毒药，格式：“毒 游戏号码”\n");
		}
		if (hasHeal) {
			sb.append("一瓶解药，格式：“救 游戏号码”\n");
			sb.append("今晚死亡情况是：\n");
			if (game.getTokill().isEmpty()) {
				sb.append("今晚没有人死亡");
			} else {
				for (Villager p : game.getTokill()) {
					sb.append(p.getMemberString());
					sb.append("\n");
				}
			}
		}
		sb.append("你可以使用其中一瓶\n如：“救 1”，\n");
		sb.append("你有一分钟的考虑时间。\n如果不需要使用药，无需发送任何内容，等待时间结束即可。");
		super.sendPrivate(sb.toString());
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (hasPoison && content.startsWith("毒")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("毒", content).replace('号', ' ').trim());
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
					if (p instanceof NightmareKnight) {
						NightmareKnight nk = (NightmareKnight) p;
						if (!nk.isSkillUsed) {
							nk.isSkillUsed = true;
							game.kill(this, DiedReason.Reflect);
						}
					}
					game.kill(p, DiedReason.Poison);
					increaseSkilledAccuracy(p.onSkilledAccuracy());
					game.logger.logSkill(this, p, "女巫毒");
					hasPoison = false;
					super.sendPrivate("毒死了" + p.getMemberString());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“毒 游戏号码”！");
				}
			} else if (hasHeal && content.startsWith("救")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("救", content).replace('号', ' ').trim());
					Villager p = game.getPlayerByNum(num);
					if (p == null) {
						super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的游戏号码已死亡，请重新输入");
						return;
					}
					if (!(game.getTokill().contains(p) && p.hasDiedReason(DiedReason.Wolf))) {
						super.sendPrivate("选择的游戏号码没有死亡危险，请重新输入。");
						return;
					}
					if (!game.isFirstNight() && p == this) {
						super.sendPrivate("你今晚无法自救！");
						return;
					}
					EndTurn();
					super.releaseListener();
					p.isSavedByWitch = true;
					hasHeal = false;
					increaseSkilledAccuracy(-p.onSkilledAccuracy());
					game.logger.logSkill(this, p, "女巫救");
					super.sendPrivate("救活了" + p.getMemberString());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“救 游戏号码”！");
				}
			}
		});
		return;
	}

	@Override
	public double onVotedAccuracy() {
		if (!hasHeal && !hasPoison)
			return 0.25;
		if (!hasHeal || !hasPoison)
			return 0.15;
		return 0;
	}

	public double onWolfKilledAccuracy() {
		return 0.5;
	}

	@Override
	public double onSkilledAccuracy() {
		if (!hasHeal && !hasPoison)
			return 0.25;
		if (!hasHeal || !hasPoison)
			return 0.15;
		return 0;
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "女巫";
	}
}
