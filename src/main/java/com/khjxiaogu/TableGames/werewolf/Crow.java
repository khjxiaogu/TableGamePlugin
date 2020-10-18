package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Member;

public class Crow extends Villager {

	public Crow(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public void onTurn() {
		super.StartTurn();
		Villager last=game.lastCursed;
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("你可以诅咒一个人，让他在明天的投票之中被额外投一票。\n请私聊选择诅咒的人，你有60秒的考虑时间。\n格式：“诅咒 qq号或者游戏号码”\n如果无需诅咒，则无需发送任何内容，等待时间结束即可。");
		ListenerUtils.registerListener(super.mid,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("诅咒")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("诅咒",content).replace('号', ' ').trim());
					Villager p=game.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if(p==last) {
						super.sendPrivate("选择的qq号或者游戏号码上次已经被诅咒，请重新输入");
						return;
					}
					this.EndTurn();
					ListenerUtils.releaseListener(super.member.getId());
					game.cursed=p;
					super.sendPrivate(p.getMemberString()+"获得了诅咒！");
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“诅咒 qq号或者游戏号码”！");
				}
			}
		});
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
		return "乌鸦";
	}
}
