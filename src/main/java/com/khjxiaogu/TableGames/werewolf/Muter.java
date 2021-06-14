package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Member;

public class Muter extends Villager {

	public Muter(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	public Muter(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，第二天晚上开始可以禁言一个人，第二天的正常发言回合，他不能发言，不能连续两晚禁言同一个人。";
	}

	@Override
	public void onTurn() {
		if(game.isFirstNight)return;
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate(
				"你可以禁言一个人，让他在明天的发言回合不能发言。\n请私聊选择禁言的人，你有60秒的考虑时间。\n格式：“禁言 qq号或者游戏号码”\n如果无需禁言，则无需发送任何内容，等待时间结束即可。");
		ListenerUtils.registerListener(super.getId(), (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("禁言")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("禁言", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if (p.lastIsMuted) {
						super.sendPrivate("选择的qq号或者游戏号码上次已经被禁言，请重新输入");
						return;
					}
					EndTurn();
					ListenerUtils.releaseListener(super.getId());
					//increaseSkilledAccuracy(p.onVotedAccuracy());
					game.logger.logSkill(this, p, "禁言");
					p.isMuted=true;
					super.sendPrivate(p.getMemberString() + "获得了禁言！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“禁言 qq号或者游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return -0.9;
	}

	@Override
	public double onSkilledAccuracy() {
		return -0.75;
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}

	@Override
	public String getRole() {
		return "禁言长老";
	}
}
