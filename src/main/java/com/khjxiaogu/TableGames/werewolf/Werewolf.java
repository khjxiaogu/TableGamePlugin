package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class Werewolf extends Villager {

	public Werewolf(WerewolfGame werewolfGame, Member member) {
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
				game.preSkipDay();
				this.onDied(DiedReason.Explode);
				game.skipDay();
			});
		} catch (Throwable t) {
			super.sendPrivate("发生错误！");
		}
	}
	@Override
	public void addDaySkillListener() {
		ListenerUtils.registerListener(mid,(msgx,typex)->{
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
		super.sendPrivate(game.getWolfSentence());
		game.vu.addToVote(this);
		ListenerUtils.registerListener(super.member,(msg,type)->{
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
					ListenerUtils.releaseListener(super.member.getId());
					ListenerUtils.registerListener(super.member, (msgx,typex)->{
						if(typex!=MsgType.PRIVATE)
							return;
						String contentx=Utils.getPlainText(msgx);
						if(contentx.startsWith("#")) {
							String tosend=this.getMemberString()+":"+Utils.removeLeadings("#",contentx);
							for(Villager w:game.playerlist) {
								if(w instanceof Werewolf&&!w.isDead&&!w.equals(this))
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
					if(w instanceof Werewolf&&!w.isDead&&!w.equals(this))
							w.sendPrivate(tosend);
				}
			}else if(content.startsWith("放弃")) {
				this.EndTurn();
				ListenerUtils.releaseListener(super.member.getId());
				game.NoVote(this);
				super.sendPrivate("已放弃");
			}
		});

	}

	@Override
	public void onGameStart() {
		super.sendPrivate("您的身份是："+getRole());
		StringBuilder sb=new StringBuilder("其他狼人身份是：\n");
		for(Villager w:game.playerlist) {
			if(w instanceof Werewolf)
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
