package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Member;

public class HiddenWolf extends Villager {

	public HiddenWolf(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
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
		super.sendPrivate(sb.toString());
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
					game.WolfVote(this,p);
					super.sendPrivate("已投票给 "+p.getMemberString());
				}catch(Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
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
	public int getTurn() {
		return 1;
	}
	@Override
	public Fraction getFraction() {
		return Fraction.Wolf;
	}
	@Override
	public String getRole() {
		return "隐狼";
	}

	@Override
	public String getPredictorRole() {
		return "平民";
	}
}
