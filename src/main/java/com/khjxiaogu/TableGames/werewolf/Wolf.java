package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.PlainText;

public class Wolf extends Innocent {

	public Wolf(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}

	@Override
	public void onWolfTurn() {
		this.sendPrivate(wereWolfGame.getAliveList());
		super.sendPrivate("请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n投票之后“#要说的话”就会失效。\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”");
		wereWolfGame.vu.addToVote(this);
		Utils.registerListener(super.member,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("投票")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("投票",content).replace('号', ' ').trim());
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
					Utils.registerListener(super.member, (msgx,typex)->{
						if(typex==MsgType.PRIVATE)
							return;
						String contentx=Utils.getPlainText(msgx);
						if(contentx.startsWith("#")) {
							String tosend=this.getMemberString()+":"+Utils.removeLeadings("#",contentx);
							for(Innocent w:wereWolfGame.playerlist) {
								if(w instanceof Wolf&&!w.isDead&&!w.equals(this))
										w.sendPrivate(tosend);
							}
						}
					});
					wereWolfGame.WolfVote(this,p);
					super.sendPrivate("已投票给 "+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("#")) {
				String tosend=this.getMemberString()+":"+Utils.removeLeadings("#",content);
				for(Innocent w:wereWolfGame.playerlist) {
					if(w instanceof Wolf&&!w.isDead&&!w.equals(this))
							w.sendPrivate(tosend);
				}
			}
		});

	}

	@Override
	public void onGameStart() {
		super.sendPrivate("您的身份是：狼人");
		StringBuilder sb=new StringBuilder("其他狼人身份是：\n");
		for(Innocent w:wereWolfGame.playerlist) {
			if(w instanceof Wolf)
				if(!w.equals(this))
					sb.append(w.getMemberString()+"\n");
		}
		sb.append("请和他们联系。");
		super.sendPrivate(sb.toString());
	}
	@Override
	public String getRole() {
		return "狼人";
	}
}
