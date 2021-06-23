package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;

import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;



public class Tramp extends Villager {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Tramp(WerewolfGame game, AbstractPlayer p) {
		super(game, p);
	}



	@Override
	public String getJobDescription() {
		return "你属于民阵营，你无论因为什么死亡，都能获得遗言机会。";
	}

	@Override
	public void onDied(DiedReason dir, boolean shouldCheckSkill) {
		// dr = dir;
		if(shouldCheckSkill) {
			onSheriffSkill();
		}
		isDead = true;
		onBeforeTalk();
		game.logger.logRaw(getNameCard() + " 老流氓出局");
		sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
		super.registerListener( (msg, type) -> {
			if (type == MsgType.AT) {
				super.releaseListener();
				game.skipWait(WaitReason.DieWord);
			}
		});
		game.startWait(300000, WaitReason.DieWord);
		sendPublic("说完了。");
		tryMute();
	}

	@Override
	public double onVotedAccuracy() {
		return 0.3;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.3;
	}

	@Override
	public String getRole() {
		return "老流氓";
	}

}
