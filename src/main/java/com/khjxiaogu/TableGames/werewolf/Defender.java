package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Member;

public class Defender extends Villager {

	public Defender(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("守卫，你可以保护一个人包括自己免于死亡，不能连续两次保护同一个人，请私聊选择保护的人，你有一分钟的考虑时间\n格式：“保护 qq号或者游戏号码”\n如：“保护 1”\n如果放弃保护，则无需发送任何内容，等待时间结束即可。");
		Utils.registerListener(super.mid,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("保护")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("保护",content).replace('号', ' ').trim());
					Villager p=game.getPlayerById(qq);
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
					this.EndTurn();
					Utils.releaseListener(super.member.getId());
					p.isGuarded=true;
					super.sendPrivate(p.getMemberString()+"获得了保护！");
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“保护 qq号或者游戏号码”！");
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
		return "守卫";
	}


}
