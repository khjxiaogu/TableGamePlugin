package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.werewolf.WereWolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.PlainText;

public class Innocent extends com.khjxiaogu.TableGames.Player {

	WereWolfGame wereWolfGame;
	boolean isDead = false;
	boolean isGuarded = false;
	boolean lastIsGuarded = false;
	boolean showRoleWhenDie = false;
	DiedReason dr = null;

	public Innocent(WereWolfGame wereWolfGame, Member member) {
		super(member);
		this.wereWolfGame = wereWolfGame;
		isDead = false;
		isGuarded = false;
		lastIsGuarded = false;
	}

	public void onGameStart() {
		sendPrivate("您的身份是：" + this.getRole() + "。");
	}

	public void onTurnStart() {
		if (lastIsGuarded) {
			lastIsGuarded = false;
		}
		if (isGuarded) {
			lastIsGuarded = true;
			isGuarded = false;
		}
	}

	public void onDayTime() {
		sendPublic("你有五分钟时间进行陈述。\n可以随时@我结束你的讲话。");
		Utils.registerListener(member, (msg, type) -> {
			if (type == MsgType.AT) {
				Utils.releaseListener(member.getId());
				wereWolfGame.skipWait();
			}
		});
		wereWolfGame.startWait(300000);
	};

	public void vote() {
		this.sendPrivate(wereWolfGame.getAliveList());
		super.sendPrivate("请私聊投票要驱逐的人，你有2分钟的考虑时间\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n弃票请输入“弃权”");
		wereWolfGame.vu.addToVote(this);
		Utils.registerListener(member, (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					Utils.releaseListener(member.getId());
					this.sendPublic("已弃权。");
					this.sendPrivate("你已弃权");
					wereWolfGame.NoVote(this);
				}
				if (content.startsWith("投票")) {
					try {
						Long qq = Long.parseLong(Utils.removeLeadings("投票", content).replace('号', ' ').trim());
						Innocent p = wereWolfGame.getPlayerById(qq);
						if (p == null) {
							super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
							return;
						}
						if (p.isDead) {
							super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
							return;
						}
						Utils.releaseListener(super.member.getId());
						super.sendPrivate("已投票给 " + p.getMemberString());
						super.sendPublic("已投票给 " + p.getMemberString());
						wereWolfGame.DayVote(this, p);
						
					} catch (Throwable t) {
						super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
					}
				}
			}
		});
	}

	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}

	public void onWolfTurn() {
	};

	public boolean onPredictorTurn() {
		return false;
	};

	public boolean onGuardTurn() {
		return false;
	};

	public boolean onWitchTurn() {
		return false;
	};

	public boolean onDiePending(DiedReason dir) {
		dr = dir;
		return false;
	};

	public void onDied(DiedReason dir) {
		dr = dir;
		if (wereWolfGame.isFirstNight || dir == DiedReason.Vote) {
			isDead = true;
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			Utils.registerListener(member, (msg, type) -> {
				if (type == MsgType.AT) {
					Utils.releaseListener(member.getId());
					wereWolfGame.skipWait();
				}
			});
			wereWolfGame.startWait(300000);
			sendPublic("说完了。");
			tryMute();
		} else {
			isDead = true;
			sendPublic("死了，没有遗言。");
			tryMute();
		}
	}

	public String getRole() {
		return "平民";
	}
}
