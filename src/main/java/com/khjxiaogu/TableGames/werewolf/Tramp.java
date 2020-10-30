package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;

import net.mamoe.mirai.contact.Member;

public class Tramp extends Villager {

	public Tramp(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onDied(DiedReason dir) {
		dr = dir;
		isDead = true;
		game.logger.logRaw(this.getNameCard()+" 老流氓出局");
		sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(getId());
				game.skipWait(WaitReason.DieWord);
			}
		});
		game.startWait(300000,WaitReason.DieWord);
		sendPublic("说完了。");
		tryMute();
	}

	@Override
	public String getRole() {
		return "老流氓";
	}

}
