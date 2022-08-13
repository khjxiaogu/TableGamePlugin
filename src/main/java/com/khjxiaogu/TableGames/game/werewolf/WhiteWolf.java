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
import com.khjxiaogu.TableGames.utils.Utils;

public class WhiteWolf extends Werewolf {

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
		return 1.3;
	}

	public WhiteWolf(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你晚上可以与其他狼人联络共同使用杀人权，但是你可以在白天投票前自爆并杀死场上一名玩家，同时立即进入黑夜。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		super.sendPrivate("白狼王，你可以在投票前随时翻牌自爆带走一个玩家并且立即进入黑夜，格式：“自爆 游戏号码”");
		super.sendPrivate(game.getAliveList());
	}
	@Override
	public void onPreSheriffSkill() {
		super.onPreSheriffSkill();
		sendPrivate("白狼王，你可以在投票前随时翻牌自爆带走一个玩家并且立即进入黑夜，格式：“自爆 游戏号码”");
		addDaySkillListener();
	}
	@Override
	public void doDaySkillPending(String content) {
		if (isDead())
			return;
		if (content.startsWith("自爆")) {
			try {
				Long num = Long.parseLong(Utils.removeLeadings("自爆", content).replace('号', ' ').trim());
				Villager p = game.getPlayerByNum(num);
				if (p == null) {
					super.sendPrivate("选择的游戏号码非游戏玩家，请重新输入");
					return;
				}
				if (p.isDead()) {
					super.sendPrivate("选择的游戏号码已死亡，请重新输入");
					return;
				}
				game.logger.logSkill(this, p, "白狼自爆");
				super.sendPublic("是白狼王，带走了" + p.getMemberString() + "进入黑夜！");
				isDead = true;
				p.isDead = true;
				game.getScheduler().execute(() -> {
					game.removeAllListeners();
					game.preSkipDay();
					p.onDied(DiedReason.Explode);
					game.logger.logDeath(p, DiedReason.Explode);
					this.onDied(DiedReason.Explode);
					game.logger.logDeath(this, DiedReason.Explode);
					
					if (game.isSheriffSelection) {
						game.isSheriffSelection = false;
						game.isSkippedDay = true;
						game.onDiePending();
						return;
					}
					game.skipDay();
				});
			} catch (Throwable t) {
				super.sendPrivate("发生错误，正确格式为：“自爆 游戏号码”！");
			}
		}
	}

	@Override
	public String getRole() {
		return "白狼王";
	}
}
