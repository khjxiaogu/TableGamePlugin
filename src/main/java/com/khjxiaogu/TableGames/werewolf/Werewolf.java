package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

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

	public Werewolf(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onTurnStart() {
		isSavedByWitch = false;
		lastIsGuarded = false;
		isArcherProtected = false;
		if (isGuarded) {
			lastIsGuarded = true;
			isGuarded = false;
		}
		addDaySkillListener();
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		super.sendPrivate(getRole()+"，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”");
	}

	@Override
	public void doDaySkillPending(String s) {
		if (isDead)
			return;
		if (s.startsWith("自爆")) {
			try {
				super.sendPublic("是狼人，自爆了，进入黑夜。");
				game.logger.logRaw(this.getNameCard()+"自爆了");
				game.getScheduler().execute(() -> {
					game.removeAllListeners();
					game.preSkipDay();
					this.onDied(DiedReason.Explode);
					if(game.isSheriffSelection) {
						game.isSheriffSelection=false;
						game.isSkippedDay=true;
						game.onDiePending();
						return;
					}
					game.skipDay();
				});
			} catch (Throwable t) {
				super.sendPrivate("发生错误！");
			}
		}
	}

	public Werewolf(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	@Override
	public void addDaySkillListener() {
		ListenerUtils.registerListener(getId(), (msgx, typex) -> {
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
		sendPrivate(game.getAliveList());
		super.sendPrivate(game.getWolfSentence());
		game.vu.addToVote(this);
		ListenerUtils.registerListener(super.getId(), (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("投票")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("投票", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					EndTurn();
					ListenerUtils.releaseListener(super.getId());
					ListenerUtils.registerListener(super.getId(), (msgx, typex) -> {
						if (typex != MsgType.PRIVATE)
							return;
						String contentx = Utils.getPlainText(msgx);
						if (contentx.startsWith("#")) {
							String tosend = getMemberString() + ":" + Utils.removeLeadings("#", contentx);
							for (Villager w : game.playerlist) {
								if (w instanceof Werewolf && !w.isDead && !w.equals(this)) {
									w.sendPrivate(tosend);
								}
							}
						}
					});
					game.logger.logSkill(this, p, "狼人投票");
					game.WolfVote(this, p);
					super.sendPrivate("已投票给 " + p.getMemberString());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
				}
			} else if (content.startsWith("#")) {
				String tosend = getMemberString() + ":" + Utils.removeLeadings("#", content);
				for (Villager w : game.playerlist) {
					if (w instanceof Werewolf && !w.isDead && !w.equals(this)) {
						w.sendPrivate(tosend);
					}
				}
			} else if (content.startsWith("放弃")) {
				EndTurn();
				ListenerUtils.releaseListener(super.getId());
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
					sb.append(w.getMemberString() + "\n");
				}
		}
		sb.append("你可以直接和他们联系，也可以通过狼人频道联系。");
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
	public Fraction getFraction() {
		return Fraction.Wolf;
	}

	@Override
	public String getRole() {
		return "狼人";
	}
}
