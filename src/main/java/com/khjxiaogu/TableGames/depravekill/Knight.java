package com.khjxiaogu.TableGames.depravekill;


import com.khjxiaogu.TableGames.depravekill.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Member;

public class Knight extends Villager {
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
			super.sendPrivate(game.getAliveList());
		}
	}
	@Override
	public void doDaySkillPending(String content) {
		if(!hasSkill)return;
		if(content.startsWith("挑战"))
		try {
			Long qq = Long.parseLong(Utils.removeLeadings("挑战", content).replace('号', ' ').trim());
			Villager p = game.getPlayerById(qq);
			if (p == null) {
				super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
				return;
			}
			if (p.isDead) {
				super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
				return;
			}
			hasSkill=false;
			game.logger.logSkill(this,p,"骑士挑战");
			super.sendPublic("是骑士，挑战了"+p.getMemberString());
			if(p instanceof NightmareKnight) {
				NightmareKnight nk=(NightmareKnight) p;
				if(!nk.isSkillUsed) {
					nk.isSkillUsed=true;
					game.sendPublicMessage(p.getMemberString()+"是恶灵骑士，同归于尽，进入黑夜。");
					p.onDied(DiedReason.Knight);
					game.kill(this, DiedReason.Reflect);
					game.skipDay();
					return;
				}
			}if(p.getFraction()==Fraction.Wolf) {
				game.logger.logDeath(p,DiedReason.Knight);
				game.sendPublicMessage(p.getMemberString()+"是狼人，被骑士杀死，进入黑夜。");
				p.onDied(DiedReason.Knight);
				game.skipDay();
			}else {
				this.isDead=true;
				game.sendPublicMessage(p.getMemberString()+"不是狼人，骑士以死谢罪。");
				game.kill(this,DiedReason.Knight_s);
			}
		} catch (Throwable t) {
			super.sendPrivate("发生错误，正确格式为：“挑战 qq号或者游戏号码”！");
		}
	}

	@Override
	public String getRole() {
		return "骑士";
	}
	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}
	@Override
	public void addDaySkillListener() {
		if(hasSkill)
			ListenerUtils.registerListener(getId(),(msgx,typex)->{
				if(typex==MsgType.PRIVATE) {
					String content=Utils.getPlainText(msgx);
					doDaySkillPending(content);
				}
			});
	}
	public Knight(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

}
