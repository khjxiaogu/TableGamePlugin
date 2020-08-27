package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.MessageListener.MsgType;

import net.mamoe.mirai.contact.Member;

public class Crow extends Innocent {

	public Crow(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}
	@Override
	public void onTurn() {
		super.StartTurn();
		Innocent last=wereWolfGame.lastCursed;
		this.sendPrivate(wereWolfGame.getAliveList());
		super.sendPrivate("你可以诅咒一个人，让他在明天的投票之中被额外投一票。\n请私聊选择诅咒的人，你有60秒的考虑时间。\n格式：“诅咒 qq号或者游戏号码”\n也可以放弃，格式：“放弃”");
		Utils.registerListener(super.mid,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("诅咒")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("诅咒",content).replace('号', ' ').trim());
					Innocent p=wereWolfGame.getPlayerById(qq);
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
					Utils.releaseListener(super.member.getId());
					wereWolfGame.cursed=p;
					super.sendPrivate(p.getMemberString()+"获得了诅咒！");
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“诅咒 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("放弃")) {
				super.sendPrivate("你放弃了诅咒。");
				this.EndTurn();
				Utils.releaseListener(super.member.getId());
			}
		});
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "乌鸦";
	}
}
