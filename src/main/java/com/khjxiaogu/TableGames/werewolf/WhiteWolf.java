package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class WhiteWolf extends WereWolf {

	public WhiteWolf(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public void onTurn() {
		super.StartTurn();
		super.sendPrivate("白狼王，你可以在投票前随时翻牌自爆带走一个玩家并且立即进入黑夜，格式：“自爆 qq号或者游戏号码”");
	}
	@Override
	public void doDaySkillPending(String content) {
		if(isDead)return;
		if(content.startsWith("自爆"))
			try {
				Long qq = Long.parseLong(Utils.removeLeadings("自爆", content).replace('号', ' ').trim());
				Villager p = game.getPlayerById(qq);
				if (p == null) {
					super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
					return;
				}
				if (p.isDead) {
					super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
					return;
				}
				super.sendPublic("是白狼王，带走了"+p.getMemberString()+"进入黑夜！");
				this.isDead=true;
				p.isDead=true;
				game.scheduler.execute(()->{
					game.removeAllListeners();
					p.onDied(DiedReason.Explode);
					this.onDied(DiedReason.Explode);
					game.skipDay();
				});
			} catch (Throwable t) {
				super.sendPrivate("发生错误，正确格式为：“自爆 qq号或者游戏号码”！");
			}
	}
	@Override
	public String getRole() {
		return "白狼王";
	}
}
