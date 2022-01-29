package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;



public class MiracleArcher extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private int lastkillId;



	public MiracleArcher(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	boolean hasArrow = true;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你有一支箭。从第二天开始，你可以消耗箭射一个人，如果他是好人，则你死亡，如果他是狼人，则他死亡；你也可以消耗箭保护一个人，如果这个人被狼人杀，则此人左手边第一个狼人死亡，同时此人存活。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		if(game.isFirstNight())
			return;
		if (!hasArrow) {
			super.sendPrivate("奇迹弓手，你没有箭了。");
			return;
		}
		sendPrivate(game.getAliveList());
		StringBuilder sb = new StringBuilder(
				"奇迹弓手，你可以射一个人，格式：“射 qq号或者游戏号码”；你可以保护一个人，格式：“保护 qq号或者游戏号码”，如果不需要使用技能，无需发送任何内容，等待时间结束即可。\n");
		sb.append("你有一分钟的考虑时间。\n");
		super.sendPrivate(sb.toString());
		super.registerListener( (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("射")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("射", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
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
					lastkillId = game.getIdByPlayer(p);
					hasArrow = false;
					super.sendPrivate("技能发动成功，请等待第二天早上结果。");
					increaseSkilledAccuracy(p.onSkilledAccuracy());
					if (p.getRealFraction() == Fraction.Wolf) {
						game.logger.logSkill(this, p, "射杀");
						game.kill(p, DiedReason.Shoot);
					} else {
						game.logger.logSkill(this, p, "射击失败");
						game.kill(this, DiedReason.Shoot_s);
					}

					super.releaseListener();
					super.EndTurn();
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“射 qq号或者游戏号码”！");
				}
			} else if (content.startsWith("保护")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("保护", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					hasArrow = false;
					p.isArcherProtected = true;
					increaseSkilledAccuracy(-p.onSkilledAccuracy());
					game.logger.logSkill(this, p, "弓手保护");
					super.sendPrivate("保护了" + p.getMemberString());
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“保护 qq号或者游戏号码”！");
				}
			}
		});
		return;
	}

	@Override
	public boolean shouldSurvive(DiedReason dir) {
		if (dir == DiedReason.Shoot_s)
			return game.getPlayerById(lastkillId).isGuarded;
		return super.shouldSurvive(dir);
	}

	@Override
	public double onVotedAccuracy() {
		if(hasArrow)
			return 0.25;
		return 0.35;
	}

	@Override
	public double onSkilledAccuracy() {
		if(hasArrow)
			return 0;
		return 0.3;
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
		return "奇迹弓手";
	}
}
