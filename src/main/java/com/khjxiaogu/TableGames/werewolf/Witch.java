package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.PlainText;

public class Witch extends Innocent {
	boolean hasPoison=true;
	boolean hasHeal=true;
	public Witch(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}
	
	@Override
	public void onGameStart() {
		sendPrivate("您的身份是：女巫。");
	}

	@Override
	public boolean onWitchTurn() {
		if(!hasHeal&&!hasPoison) {
			super.sendPrivate("女巫，你没有药了。");
			return false;
		}
		this.sendPrivate(wereWolfGame.getAliveList());
		StringBuilder sb=new StringBuilder("女巫，你有");
		if(hasPoison) {
			sb.append("一瓶毒药，可以杀死一个人，格式：“毒 qq号或者游戏号码”\n");
		}
		if(hasHeal) {
			sb.append("一瓶解药，可以救活一个人，格式：“救 qq号或者游戏号码”\n");
			sb.append("今晚死亡情况是：\n");
			if(wereWolfGame.tokill.isEmpty()) {
				sb.append("今晚没有人死亡");
			}else {
				for(Innocent p:wereWolfGame.tokill.keySet()) {
					sb.append(p.getMemberString());
					sb.append("\n");
				}
			}
		}
		sb.append("你可以使用其中一瓶\n如：“救 1”，\n也可以放弃，格式：“放弃”\n");
		sb.append("你有30秒的考虑时间。");
		super.sendPrivate(sb.toString());
		Utils.registerListener(super.member,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(hasPoison&&content.startsWith("毒")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("毒",content).replace('号', ' ').trim());
					Innocent p=wereWolfGame.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					Utils.releaseListener(super.member.getId());
					wereWolfGame.witchKill(p);
					hasPoison=false;
					super.sendPrivate("毒死了"+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“毒 qq号或者游戏号码”！");
				}
			}else if(hasHeal&&content.startsWith("救")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("救",content).replace('号', ' ').trim());
					Innocent p=wereWolfGame.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if(!wereWolfGame.tokill.containsKey(p)) {
						super.sendPrivate("选择的qq号或者游戏号码没有死亡危险，请重新输入。");
						return;
					}
					if((!wereWolfGame.isFirstNight)&&p==this) {
						super.sendPrivate("你今晚无法自救！");
						return;
					}
					Utils.releaseListener(super.member.getId());
					wereWolfGame.witchProtect(p);
					hasHeal=false;
					super.sendPrivate("救活了"+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“救 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("放弃")) {
				super.sendPrivate("你放弃了使用药。");
				Utils.releaseListener(super.member.getId());
			}
		});
		return true;
	}
	@Override
	public String getRole() {
		return "女巫";
	}
}
