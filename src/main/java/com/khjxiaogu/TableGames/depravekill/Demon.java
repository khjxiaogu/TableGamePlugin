package com.khjxiaogu.TableGames.depravekill;

import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Member;

public class Demon extends Villager {

	public Demon(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("魔镜魅魔，你可以查验一个人的身份，请私聊选择查验的人，你有30秒的考虑时间\n格式：“查验 qq号或者游戏号码”\n如：“查验 1”");
		ListenerUtils.registerListener(super.getId(),(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("查验")) {
				try {
					Long qq=Long.parseLong(Utils.removeLeadings("查验",content).replace('号', ' ').trim());
					Villager p=game.getPlayerById(qq);
					if(p==null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if(p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					if(p==this) {
						super.sendPrivate("不能查验自己！");
						return;
					}
					if(p.isDemonChecked) {
						super.sendPrivate("此玩家已经被查验过！");
						return;
					}
					this.EndTurn();
					ListenerUtils.releaseListener(super.getId());
					game.logger.logSkill(this,p,"魔镜魅魔查验");
					p.isDemonChecked=true;
					super.sendPrivate(p.getMemberString()+"是"+p.getPredictorRole());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“查验 qq号或者游戏号码”！");
				}
			}
		});
	}

	@Override
	public void onWolfTurn() {
		for(Villager inno:game.playerlist) {
			if(inno instanceof Werewolf&&!inno.isDead)
				return;
		}
		for(Villager inno:game.playerlist) {
			if(inno==this)break;
			if(inno.getFraction()==Fraction.Wolf&&!inno.isDead)
				return;
		}
		this.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate(game.getWolfSentence());
		game.vu.addToVote(this);
		ListenerUtils.registerListener(super.getId(),(msg,type)->{
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
					ListenerUtils.releaseListener(super.getId());
					game.WolfVote(this,p);
					game.logger.logSkill(this,p,"狼人投票");
					super.sendPrivate("已投票给 "+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
				}
			}else if(content.startsWith("放弃")) {
				this.EndTurn();
				ListenerUtils.releaseListener(super.getId());
				game.NoVote(this);
				super.sendPrivate("已放弃");
			}
		});
	}

	@Override
	public int getTurn() {
		return 2;
	}
	@Override
	public Fraction getFraction() {
		return Fraction.Wolf;
	}
	@Override
	public String getRole() {
		return "魔镜魅魔";
	}

	@Override
	public String getPredictorRole() {
		return "魅魔";
	}

}
