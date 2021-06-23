package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;

import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;


public class Fox extends Villager {



	public Fox(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	boolean canSkill=true;
	@Override
	public String getJobDescription() {
		return "你属于神阵营，你每晚可以选择一个人来查验这个人和他前后两侧有无狼人，如果无狼人，你失去技能。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		if(!canSkill)return;
		sendPrivate(game.getAliveList());
		super.sendPrivate("狐狸，你可以选择查验一个人，请私聊选择查验的人，你有一分钟的考虑时间\n格式：“查验 qq号或者游戏号码”\n如：“查验 1”");
		super.registerListener( (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("查验")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("查验", content).replace('号', ' ').trim());
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
					super.releaseListener();
					game.logger.logSkill(this, p, "查验");
					if(p.getFraction()==Fraction.Wolf) {
						game.logger.logRaw("狐狸查到狼人");
						sendPrivate("三人中存在狼人。");
						return;
					}
					Villager pt = p;
					while (true) {
						pt = pt.prev;
						if (pt.isDead) {
							continue;
						}
						if (pt.getFraction() == Fraction.Wolf) {
							game.logger.logRaw("狐狸查到狼人");
							sendPrivate("三人中存在狼人。");
							return;
						}
						break;
					}
					pt = p;
					while (true) {
						pt = pt.next;
						if (pt.isDead) {
							continue;
						}
						if (pt.getFraction() == Fraction.Wolf) {
							game.logger.logRaw("狐狸查到狼人");
							sendPrivate("三人中存在狼人。");
							return;
						}
						break;
					}
					canSkill=false;
					sendPrivate("三人中不存在狼人，你失去了技能。");
					game.logger.logRaw("狐狸失去技能");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“查验 qq号或者游戏号码”！");
				}
			}
		});
	}

	@Override
	public double onVotedAccuracy() {
		return 0.5;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.5;
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
		return "狐狸";
	}
}
