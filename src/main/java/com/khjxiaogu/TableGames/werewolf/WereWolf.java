package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.Utils;

import net.mamoe.mirai.contact.Member;

public class WereWolf extends Villager {

	public WereWolf(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public void onTurnStart() {
		super.onTurnStart();
		onTurn();
	}
	@Override
	public void onTurn() {
		super.StartTurn();
		super.sendPrivate("狼人，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”\n");
	}
	@Override
	public void doDaySkillPending(String s) {
		if(isDead)return;
		if(s.startsWith("自爆"))
		try {
			super.sendPublic("是狼人，自爆了，进入黑夜。");
			game.scheduler.execute(()->{
				game.removeAllListeners();
				this.onDied(DiedReason.Explode);
				game.skipDay();
			});
		} catch (Throwable t) {
			super.sendPrivate("发生错误！");
		}
	}
	@Override
	public void addDaySkillListener() {
		Utils.registerListener(mid,(msgx,typex)->{
			if(typex==MsgType.PRIVATE) {
				String content=Utils.getPlainText(msgx);
				this.doDaySkillPending(content);
			}
		});
	}
	@Override
	public void onWolfTurn() {
		this.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n投票之后“#要说的话”就会失效。\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”");
		game.vu.addToVote(this);
		Utils.registerListener(super.member,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("投票")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("投票",content).replace('号', ' ').trim());
					Villager p=game.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					this.EndTurn();
					Utils.releaseListener(super.member.getId());
					Utils.registerListener(super.member, (msgx,typex)->{
						if(typex==MsgType.PRIVATE)
							return;
						String contentx=Utils.getPlainText(msgx);
						if(contentx.startsWith("#")) {
							String tosend=this.getMemberString()+":"+Utils.removeLeadings("#",contentx);
							for(Villager w:game.playerlist) {
								if(w instanceof WereWolf&&!w.isDead&&!w.equals(this))
										w.sendPrivate(tosend);
							}
						}
					});
					game.WolfVote(this,p);
					super.sendPrivate("已投票给 "+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("#")) {
				String tosend=this.getMemberString()+":"+Utils.removeLeadings("#",content);
				for(Villager w:game.playerlist) {
					if(w instanceof WereWolf&&!w.isDead&&!w.equals(this))
							w.sendPrivate(tosend);
				}
			}
		});

	}

	@Override
	public void onGameStart() {
		super.sendPrivate("您的身份是："+getRole());
		StringBuilder sb=new StringBuilder("其他狼人身份是：\n");
		for(Villager w:game.playerlist) {
			if(w instanceof WereWolf)
				if(!w.equals(this))
					sb.append(w.getMemberString()+"\n");
		}
		sb.append("你可以直接和他们联系，也可以通过狼人频道联系。");
		super.sendPrivate(sb.toString());
	}
	@Override
	public int getTurn() {
		return 1;
	}
	@Override
	public Fraction getFraction() {
		return Fraction.Wolf;
	}
	@Override
	public String getRole() {
		return "狼人";
	}
}
