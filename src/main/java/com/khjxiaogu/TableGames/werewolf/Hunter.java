package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.message.Text;

import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;


public class Hunter extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public double onVotedAccuracy() {
		return -0.75;
	}

	@Override
	public double onSkilledAccuracy() {
		return -0.9;
	}

	public Hunter(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	boolean hasGun = true;
	boolean asked = false;

	@Override
	public String getJobDescription() {
		return "你属于神阵营，你被投票或者被狼人杀死时可以开枪打死一个人。要注意的是每天晚上死亡技能回合只有30秒时间，请注意提前考虑好。";
	}

	@Override
	public void onDieSkill(DiedReason dir) {
		super.StartTurn();
		// dr = dir;

		sendPrivate(game.getAliveList());
		if (!game.isDayTime) {
			super.sendPrivate("猎人，你死了，你可以选择翻牌并开枪打死另一个人，你有30秒的考虑时间\n格式：“杀死 qq号或者游戏号码”\n如：“杀死 1”\n如果不需要，则等待时间结束即可。");
		} else {
			super.sendPrivate("猎人，你死了，你可以选择翻牌并开枪打死另一个人，也可以不开枪\n格式：“杀死 qq号或者游戏号码”\n如：“杀死 1”。\n如果放弃开枪，请输入：“放弃”。");
		}
		asked = true;
		super.registerListener( (msg, type) -> {
			if (type == MsgType.AT) {
				if(dir == DiedReason.Vote || dir == DiedReason.Explode) {
					if(hasGun) {
						super.sendPrivate("你还没选择发动技能，如果不需要发动，请手动输入“放弃”，否则不能结束遗言！");
						return;
					}
				}else if (!game.isFirstNight)
					return;
				super.releaseListener();
				game.skipWait(WaitReason.DieWord);
			}
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("杀死")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("杀死", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if (p.equals(this)) {
						super.sendPrivate("选择的qq号或者游戏号码是你自己，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					if (dir == DiedReason.Vote || dir == DiedReason.Explode) {
						super.registerListener( (msgx, typex) -> {
							if (typex == MsgType.AT) {
								super.releaseListener();
								game.skipWait(WaitReason.DieWord);
							}
						});
					}
					hasGun = false;
					game.logger.logSkill(this, p, "猎人杀死");
					super.sendPrivate("你杀死了" + p.getMemberString());
					if (game.isDayTime) {
						super.sendPrivate("你的遗言回合还未结束，请在说完后在群里@机器人结束。");
					}
					boolean hasDarkWolf = false;
					for (Villager vill : game.playerlist) {
						if (vill instanceof DarkWolf) {
							hasDarkWolf = true;
							break;
						}
					}
					increaseSkilledAccuracy(p.onSkilledAccuracy());
					if (!hasDarkWolf) {
						super.sendPublic(new Text("死亡，身份是猎人，同时带走了").asMessage().append(p.getAt()));
					} else {
						super.sendPublic(new Text("死亡，同时带走了").asMessage().append(p.getAt()));
					}
					p.isDead = true;
					game.kill(p, DiedReason.Hunter);
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“杀死 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("放弃")) {
				hasGun=false;
				super.sendPrivate("你放弃了开枪。");
			}
		});
	}
	@Override
	public boolean shouldWaitDeathSkill() {
		return true;
	}
	@Override
	public boolean canDeathSkill(DiedReason dir) {
		if (hasGun && !asked && dir.canUseSkill)
			return true;
		return super.canDeathSkill(dir);
	}

	@Override
	public void onTurn() {
		onDieSkill(getEffectiveDiedReason());
	}

	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}



	@Override
	public int getTurn() {
		return 3;
	}

	@Override
	public String getRole() {
		return "猎人";
	}
}
