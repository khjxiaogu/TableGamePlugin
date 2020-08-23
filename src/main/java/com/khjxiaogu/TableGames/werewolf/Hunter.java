package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.werewolf.WereWolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.PlainText;

public class Hunter extends Innocent {
	@Override
	public boolean onDiePending(DiedReason dir) {
		dr=dir;
		if(dir!=DiedReason.Poison) {
			this.sendPrivate(wereWolfGame.getAliveList());
			super.sendPrivate("猎人，你死了，你可以选择暴露并开枪打死另一个人，你有30秒的考虑时间\n格式：“杀死 qq号或者游戏号码”\n如：“杀死 1”\n也可以放弃，格式：“放弃”");
			Utils.registerListener(super.member,(msg,type)->{
				if(dir==DiedReason.Vote&&type==MsgType.AT) {
					Utils.releaseListener(member.getId());
					wereWolfGame.skipWait();
				}
				if(type!=MsgType.PRIVATE)return;
				String content=Utils.getPlainText(msg);
				if(content.startsWith("杀死")) {
					try {
						Long qq=Long.parseLong(Utils.removeLeadings("杀死",content).replace('号', ' ').trim());
						Innocent p=wereWolfGame.getPlayerById(qq);
						if(p==null) {
							super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
							return;
						}
						if(p.isDead) {
							super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
							return;
						}
						if(p.equals(this)) {
							super.sendPrivate("选择的qq号或者游戏号码是你自己，请重新输入");
							return;
						}
						Utils.releaseListener(super.member.getId());
						if(dir==DiedReason.Vote)
							Utils.registerListener(super.member, (msgx,typex)->{
								if(typex==MsgType.AT) {
									Utils.releaseListener(member.getId());
									wereWolfGame.skipWait();
								}
							});
						super.sendPrivate("你杀死了"+p.getMemberString());
						super.sendPublic(new PlainText("死亡，身份是猎人，同时带走了").plus(p.getAt()));
						if(dir==DiedReason.Vote)
							wereWolfGame.hunterKillForce(p);
						else
							wereWolfGame.hunterKill(p);
					}catch(Throwable t) {
						super.sendPrivate("发生错误，正确格式为：“杀死 qq号或者游戏号码”！");
					}
				}else if(content.startsWith("放弃")) {
					super.sendPrivate("你放弃了开枪！");
				}
			});
			return true;
		}
		return false;
	}

	@Override
	public void onDied(DiedReason dir) {
		dr=dir;
		if(dir==DiedReason.Vote){
			isDead=true;
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			this.onDiePending(dir);
			wereWolfGame.startWait(300000);
			sendPublic("说完了。");
			super.tryMute();
		}else
			super.onDied(dir);
	}

	public Hunter(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}
	@Override
	public String getRole() {
		return "猎人";
	}
}
