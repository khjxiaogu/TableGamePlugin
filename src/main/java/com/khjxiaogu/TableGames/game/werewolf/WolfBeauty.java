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

public class WolfBeauty extends Werewolf {
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
		return 1.25;
	}

	public WolfBeauty(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	int pid = -1;

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你每晚除了杀人外可以额外魅惑一个人，当你白天受到伤害时此人同时出局。";
	}

	@Override
	public void onTurn() {
		pid = -1;
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate("狼美人，你可以魅惑一个好人。\n格式：“魅惑 游戏号码”\n如：“魅惑 1”\n如果放弃魅惑，则无需发送任何内容，等待时间结束即可。");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("魅惑")) {
				try {
					Long num = Long.parseLong(Utils.removeLeadings("魅惑", content).replace('号', ' ').trim());
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
					game.logger.logSkill(this, p, "狼美人魅惑");
					EndTurn();
					super.releaseListener();
					if (!(p instanceof Tramp)) {
						pid = game.getIdByPlayer(p);
					}
					super.sendPrivate(p.getMemberString() + "获得了魅惑！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“魅惑 游戏号码”！");
				}
			}
		});
	}

	@Override
	public void onDieSkill(DiedReason dir) {
		if (dir == DiedReason.Vote || dir == DiedReason.Hunter || dir == DiedReason.Knight
				|| dir == DiedReason.Explode) {
			if (pid == -1)
				return;
			Villager p = game.getPlayerByNum(pid);
			if (p == null || p.isDead())
				return;
			game.logger.logSkill(p, this, "殉情");
			p.sendPublic("殉情了。");
			game.kill(p, DiedReason.Love);
			game.logger.logDeath(p, DiedReason.Love);
		}
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
	public void onDied(DiedReason dir) {
		if (dir == DiedReason.Vote || dir == DiedReason.Hunter || dir == DiedReason.Knight
				|| dir == DiedReason.Explode) {
			if (pid == -1)
				return;
			Villager p = game.getPlayerByNum(pid);
			if (p == null || p.isDead())
				return;
			game.logger.logSkill(p, this, "殉情");
			p.sendPublic("殉情了。");
			game.kill(p, DiedReason.Love);
			game.logger.logDeath(p, DiedReason.Love);
		}
		super.onDied(dir);
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
		return "狼美人";
	}

}
