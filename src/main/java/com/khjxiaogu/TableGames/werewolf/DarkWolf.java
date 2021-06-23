package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.message.Text;

import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;

public class DarkWolf extends Werewolf {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public String getJobDescription() {
		return "你属于狼人阵营，你晚上可以与其他狼人联络共同使用杀人权，若你被投出局则可以选择杀死另一个人。";
	}

	public DarkWolf(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}

	@Override
	public String getRole() {
		return "狼王";
	}

	boolean hasGun = true;
	boolean asked = false;

	@Override
	public void onDieSkill(DiedReason dir) {
		super.StartTurn();
		// dr = dir;
		sendPrivate(game.getAliveList());
		super.sendPrivate("狼王，你死了，你可以选择打死另一个人，你有30秒的考虑时间\n格式：“杀死 qq号或者游戏号码”\n如：“杀死 1”\n也可以放弃，格式：“放弃”");
		asked = true;
		super.registerListener( (msg, type) -> {
			if ((dir == DiedReason.Vote || game.isFirstNight) && type == MsgType.AT) {
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
						super.registerListener((msgx, typex) -> {
							if (typex == MsgType.AT) {
								super.releaseListener();
								game.skipWait(WaitReason.DieWord);
							}
						});
					}
					hasGun = false;
					super.sendPrivate("你杀死了" + p.getMemberString());
					game.logger.logSkill(this, p, "狼王杀死");
					super.sendPublic(new Text("死亡，同时带走了").asMessage().append(p.getAt()));
					p.isDead = true;
					game.kill(p, DiedReason.DarkWolf);
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“杀死 qq号或者游戏号码”！");
				}
			} else if (content.startsWith("放弃")) {
				super.sendPrivate("你放弃了开枪！");
			}
		});
	}
	@Override
	public boolean shouldWaitDeathSkill() {
		return true;
	}
	@Override
	public double onVotedAccuracy() {
		return 1.25;
	}

	@Override
	public double onSkilledAccuracy() {
		return 1.5;
	}

	@Override
	public boolean canDeathSkill(DiedReason dir) {
		if (hasGun && !asked && dir.canUseSkill)
			return true;
		return super.canDeathSkill(dir);
	}

}
