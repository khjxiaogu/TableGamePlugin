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
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.WaitReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.mirai.message.MiraiMessageCompound;
import com.khjxiaogu.TableGames.utils.Utils;

public class Werewolf extends Villager {

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
		return 1;
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		super.sendPrivate(getMemberString()+"，你是"+getRole() + "，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”");
	}
	public void onTurnStart() {
		super.onTurnStart();
		super.sendPrivate(getMemberString()+"，你是"+getRole() + "，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”");
	}
	public void SheriffDeselect(IMessageCompound msg, MsgType type) {
		if (type == MsgType.PRIVATE) {
			if (Utils.getPlainText(msg).startsWith("退选")) {
				game.logger.logRaw(this.getMemberString(this) + "已退选");
				this.sendForName("已退选");
				game.sherifflist.remove(this);
				this.releaseListener();
				this.addDaySkillListener();
			} else {
				doDaySkillPending(Utils.getPlainText(msg));
			}
		}
	}
	public void onSheriffState() {
		onBeforeTalk();
		sendPublic("你有五分钟时间进行竞选发言。\n可以随时@我结束你的讲话。");
		super.registerListener((msg, type) -> {
			if (type == MsgType.AT) {
				super.releaseListener();
				super.registerListener((msgx, typex) -> SheriffDeselect(msgx, typex));
				game.skipWait(WaitReason.State);
			} else if (type == MsgType.PRIVATE) {
				if(Utils.getPlainText(msg).startsWith("退选")) {
					game.logger.logRaw(this.getMemberString(this) + "已退选");
					this.sendForName("已退选");
					game.sherifflist.remove(this);
					this.releaseListener();
					addDaySkillListener();
					game.skipWait(WaitReason.State);
				}else doDaySkillPending(Utils.getPlainText(msg));
			}
		});
		game.startWait(300000, WaitReason.State);
		onFinishTalk();
		super.registerListener((msgx, typex) -> SheriffDeselect(msgx, typex));
	}
	@Override
	public void doDaySkillPending(String s) {
		if (isDead())
			return;
		if (s.startsWith("自爆")) {
			try {
				this.canContinueState = true;
				sendForName("是狼人，自爆了，进入黑夜。");
				game.logger.logRaw(this.getMemberString(this) + "自爆了");
				game.getScheduler().execute(() -> {
					game.removeAllListeners();
					game.preSkipDay();
					this.onDied(DiedReason.Explode);
					if (game.isSheriffSelection) {
						game.isSheriffSelection = false;
						game.isSkippedDay = true;
						game.onDiePending();
						return;
					}
					this.canContinueState = false;
					game.skipDay();
				});
			} catch (Throwable t) {
				this.canContinueState = false;
				super.sendPrivate("发生错误！");
			}
		}
	}

	public Werewolf(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	@Override
	public void addDaySkillListener() {
		super.registerListener((msgx, typex) -> {
			if (typex == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msgx);
				doDaySkillPending(content);
			}
		});
	}

	@Override
	public boolean canWolfTurn() {
		return true;
	}

	@Override
	public void onWolfTurn() {
		StartTurn();
		sendPrivate(game.getAliveList(this));
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
					super.registerListener((msgx, typex) -> {
						if (typex != MsgType.PRIVATE)
							return;
						String contentx = Utils.getPlainText(msgx);
						if (contentx.startsWith("#")) {
							String tosendHead = this.getMemberString(this);
							String tosendEnd=":" + Utils.removeLeadings("#", contentx);
							for (Villager w : game.playerlist) {
								if (w instanceof Werewolf && !w.isDead() && !w.equals(this)) {
									w.sendPrivate(tosendHead+"->"+w.getMemberString()+tosendEnd);
								}
							}
						}
					});
					game.logger.logSkill(this, p, "狼人投票");
					game.WolfVote(this, p);
					super.sendPrivate("已投票给 " + p.getMemberString(this));
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 游戏号码”！");
				}
			} else if (content.startsWith("#")) {
				String tosendHead = this.getMemberString(this);
				String tosendEnd= Utils.removeLeadings("#", content);
				for (Villager w : game.playerlist) {
					if (w instanceof Werewolf && !w.isDead() && !w.equals(this)) {
						w.sendPrivate(tosendHead+":"+tosendEnd);
					}
				}
			} else if (content.startsWith("放弃")) {
				EndTurn();
				super.releaseListener();
				super.registerListener((msgx, typex) -> {
					if (typex != MsgType.PRIVATE)
						return;
					String contentx = Utils.getPlainText(msgx);
					if (contentx.startsWith("#")) {
						String tosendHead = this.getMemberString(this);
						String tosendEnd=":" + Utils.removeLeadings("#", contentx);
						for (Villager w : game.playerlist) {
							if (w instanceof Werewolf && !w.isDead() && !w.equals(this)) {
								w.sendPrivate(tosendHead+"->"+w.getMemberString()+tosendEnd);
							}
						}
					}
				});
				game.NoVote(this);
				super.sendPrivate("已放弃");
			}
		});

	}

	@Override
	public void onGameStart() {
		super.sendPrivate("您的身份是：" + getRole());
		super.sendPrivate(getJobDescription());
		StringBuilder sb = new StringBuilder("其他狼人身份是：\n");
		for (Villager w : game.playerlist) {
			if (w instanceof Werewolf)
				if (!w.equals(this)) {
					sb.append(w.index + "号 |" + w.origname + "\n");
				}
		}
		sb.append("你可以直接和他们联系，也可以通过狼人频道联系，狼人频道仅当狼人回合时有效！");
		super.sendPrivate(sb.toString());

	}

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你的目的是合作把所有好人去除。";
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
		return "狼人";
	}

	@Override
	public void onPreSheriffSkill() {
		super.onPreSheriffSkill();
		sendPrivate(getMemberString()+"，你是"+getRole() + "，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”");
		addDaySkillListener();
	}
	public String getMemberString(Villager to) {
		if(game.isNameProtected&&!(to instanceof Werewolf))
			return index+"号 |"+index+"号玩家";
		return index+"号 |"+origname;
	}
}
