package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class WolfKiller extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public WolfKiller(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	public WolfKiller(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	private int lastkillId;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你从第二晚开始每晚可以狩猎一个人，如果这个人是狼人，狼人出局；如果这个人是好人，则你出局。";
	}

	@Override
	public void onTurn() {
		if (game.isFirstNight)
			return;
		super.StartTurn();
		sendPrivate(game.getAliveList());
		super.sendPrivate("猎魔人，你可以选择狩猎一个人。\n格式：“猎杀 qq号或者游戏号码”\n或者可以放弃，格式：“放弃”");
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("猎杀")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("猎杀", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					lastkillId = game.getIdByPlayer(p);
					super.sendPrivate("技能发动成功，请等待第二天早上结果。");
					increaseSkilledAccuracy(p.onSkilledAccuracy());
					if (p.getFraction() == Fraction.Wolf) {
						game.logger.logSkill(this, p, "猎杀");
						game.kill(p, DiedReason.Hunt);
					} else {
						game.logger.logSkill(this, p, "猎杀失败");
						game.kill(this, DiedReason.Hunt_s);
					}
					ListenerUtils.releaseListener(getId());
					super.EndTurn();
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“猎杀 qq号或者游戏号码”！");
				}
			}
			if (content.startsWith("放弃")) {
				ListenerUtils.releaseListener(getId());
				super.sendPrivate("您已经放弃");
				super.EndTurn();
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return -1.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return -1.4;
	}

	@Override
	public boolean shouldReplace(DiedReason src, DiedReason dest) {
		if (dest != DiedReason.Poison)
			return super.shouldReplace(src, dest);
		return false;
	}

	@Override
	public boolean shouldSurvive(DiedReason dir) {
		if (dir == DiedReason.Hunt_s)
			return game.getPlayerById(lastkillId).isGuarded;
		if (dir == DiedReason.Poison)
			return true;
		return super.shouldSurvive(dir);
	}

	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "猎魔人";
	}

}
