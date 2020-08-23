package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;

import net.mamoe.mirai.contact.Member;

public class Guard extends Innocent {

	public Guard(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}

	@Override
	public void onGameStart() {
		sendPrivate("您的身份是：守卫。");
	}

	@Override
	public boolean onGuardTurn() {
		this.sendPrivate(wereWolfGame.getAliveList());
		super.sendPrivate("守卫，你可以保护一个人包括自己免于死亡，不能连续两次保护同一个人，请私聊选择保护的人，你有30秒的考虑时间\n格式：“保护 qq号或者游戏号码”\n如：“保护 1”");
		Utils.registerListener(super.member,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("保护")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("保护",content).replace('号', ' ').trim());
					Innocent p=wereWolfGame.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if(p.lastIsGuarded) {
						super.sendPrivate("选择的qq号或者游戏号码上次已经被保护，请重新输入");
						return;
					}
					Utils.releaseListener(super.member.getId());
					p.isGuarded=true;
					wereWolfGame.tokill.remove(p);
					super.sendPrivate(p.getMemberString()+"获得了保护！");
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“保护 qq号或者游戏号码”！");
				}
			}
		});
		return true;
	}

	@Override
	public String getRole() {
		return "守卫";
	}


}
