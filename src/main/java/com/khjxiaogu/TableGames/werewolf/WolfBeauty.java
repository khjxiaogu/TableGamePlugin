package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class WolfBeauty extends Werewolf {
	Villager p;
	public WolfBeauty(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("狼美人，你可以魅惑一个好人，当你白天受伤害时此人同时出局。\n格式：“魅惑 qq号或者游戏号码”\n如：“魅惑 1”\n如果放弃魅惑，则无需发送任何内容，等待时间结束即可。");
		Utils.registerListener(super.mid,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("魅惑")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("魅惑",content).replace('号', ' ').trim());
					Villager p=game.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if(p instanceof Werewolf) {
						super.sendPrivate("选择的qq号或者游戏号码是狼人，请重新输入");
						return;
					}
					this.EndTurn();
					Utils.releaseListener(super.member.getId());
					this.p=p;
					super.sendPrivate(p.getMemberString()+"获得了魅惑！");
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“魅惑 qq号或者游戏号码”！");
				}
			}
		});
	}

	@Override
	public boolean onDiePending(DiedReason dir) {
		if(dir==DiedReason.Vote||dir==DiedReason.Hunter||dir==DiedReason.Knight||dir==DiedReason.Explode) {
			super.sendPublic(p.getAt()+"殉情了。");
			p.onDied(DiedReason.Love);
		}
		return super.onDiePending(dir);
	}

	@Override
	public void onDied(DiedReason dir) {
		if(dir==DiedReason.Vote||dir==DiedReason.Hunter||dir==DiedReason.Knight||dir==DiedReason.Explode) {
			super.sendPublic(p.getAt()+"殉情了。");
			p.onDied(DiedReason.Love);
		}
		super.onDied(dir);
	}

	@Override
	public void doDaySkillPending(String s) {
	}

	@Override
	public void addDaySkillListener() {
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "狼美人";
	}

}