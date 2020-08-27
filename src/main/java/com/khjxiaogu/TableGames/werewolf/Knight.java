package com.khjxiaogu.TableGames.werewolf;


import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.werewolf.WereWolfGame.DiedReason;
import com.khjxiaogu.TableGames.MessageListener.MsgType;

import net.mamoe.mirai.contact.Member;

public class Knight extends Innocent {
	boolean hasSkill=true;
	@Override
	public void onTurnStart() {
		super.onTurnStart();
		onTurn();
	}
	@Override
	public void onTurn() {
		super.StartTurn();
		if(hasSkill) {
			super.sendPrivate("骑士，你可以翻牌挑战一个人。\n如果这个人是狼人，狼人死，立即进入黑夜，你失去技能。\n如果这个人是好人，你死。\n你可以在投票前随时使用本技能。\n格式：“挑战 qq号或者游戏号码”\n");
		}
	}
	@Override
	public void doDaySkillPending(String content) {
		if(!hasSkill)return;
		if(content.startsWith("挑战"))
		try {
			Long qq = Long.parseLong(Utils.removeLeadings("挑战", content).replace('号', ' ').trim());
			Innocent p = wereWolfGame.getPlayerById(qq);
			if (p == null) {
				super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
				return;
			}
			if (p.isDead) {
				super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
				return;
			}
			hasSkill=false;
			super.sendPublic("是骑士，挑战了"+p.getMemberString());
			if(p instanceof Wolf) {
				wereWolfGame.sendPublicMessage(p.getMemberString()+"是狼人，被骑士杀死，进入黑夜。");
				p.onDied(DiedReason.Knight);
				wereWolfGame.skipDay();
			}else {
				this.isDead=true;
				wereWolfGame.sendPublicMessage(p.getMemberString()+"不是狼人，骑士以死谢罪。");
				wereWolfGame.kill(this,DiedReason.Knight_s);
			}
		} catch (Throwable t) {
			super.sendPrivate("发生错误，正确格式为：“挑战 qq号或者游戏号码”！");
		}
	}
	@Override
	public void onDied(DiedReason dir) {
		dr = dir;
		if (wereWolfGame.isFirstNight || dir == DiedReason.Vote) {
			isDead = true;
			if(hasSkill)
			super.sendPrivate("骑士，你可以翻牌挑战一个人。\n如果这个人是狼人，狼人死，立即进入黑夜，你失去技能。\n如果这个人是好人，你死。\n你可以在结束遗言前随时使用本技能。\n格式：“挑战 qq号或者游戏号码”\n");
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			Utils.registerListener(member, (msg, type) -> {
				if (type == MsgType.AT) {
					Utils.releaseListener(member.getId());
					wereWolfGame.skipWait();
				}else if (type == MsgType.PRIVATE) {
					doDaySkillPending(Utils.getPlainText(msg));
				}
			});
			wereWolfGame.startWait(300000);
			sendPublic("说完了。");
			tryMute();
		} else {
			isDead = true;
			sendPublic("死了，没有遗言。");
			tryMute();
		}
	}
	@Override
	public String getRole() {
		return "骑士";
	}
	@Override
	public void addDaySkillListener() {
		if(hasSkill)
			Utils.registerListener(mid,(msgx,typex)->{
				if(typex==MsgType.PRIVATE) {
					String content=Utils.getPlainText(msgx);
					doDaySkillPending(content);
				}
			});
	}
	public Knight(WereWolfGame wereWolfGame, Member member) {
		super(wereWolfGame, member);
	}

}
