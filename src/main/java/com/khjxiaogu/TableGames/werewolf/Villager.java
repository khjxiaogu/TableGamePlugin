package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.Behaviour.SkillBehaviour;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;

import net.mamoe.mirai.contact.Member;

public class Villager extends com.khjxiaogu.TableGames.Player {

	WerewolfGame game;
	boolean isDead = false;
	boolean isGuarded = false;
	boolean lastIsGuarded = false;
	boolean showRoleWhenDie = false;
	boolean isCurrentTurn=false;
	boolean isVoteTurn=false;
	boolean isSavedByWitch=false;
	boolean isDemonChecked=false;
	boolean isSheriff=false;
	boolean isExchanged=false;
	DiedReason dr = null;
	Villager prev;
	Villager next;
	Behaviour vote;
	Behaviour desc;
	Behaviour dead;
	Behaviour[] skills=new Behaviour[4];
	public Villager(WerewolfGame werewolfGame, Member member) {
		super(member);
		this.game = werewolfGame;
		isDead = false;
		isGuarded = false;
		lastIsGuarded = false;
	}

	public void onGameStart() {
		sendPrivate("您的身份是：" + this.getRole() + "。");
	}

	public void onTurnStart() {
		isSavedByWitch=false;
		lastIsGuarded = false;
		if (isGuarded) {
			lastIsGuarded = true;
			isGuarded = false;
		}
		addDaySkillListener();
	}
	public void addDaySkillListener() {
	}
	public void onDayTime() {
		sendPublic("你有五分钟时间进行陈述。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(member, (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(member.getId());
				addDaySkillListener();
				game.skipWait(WaitReason.State);
			}else if(type==MsgType.PRIVATE) {
				doDaySkillPending(Utils.getPlainText(msg));
			}
		});
		game.startWait(300000,WaitReason.State);
	};

	public void doDaySkillPending(String plainText) {
	}

	public void vote() {
		isVoteTurn=true;
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("请私聊投票要驱逐的人，你有2分钟的考虑时间\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n弃票请输入“弃权”");
		game.vu.addToVote(this);
		ListenerUtils.registerListener(member, (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					isVoteTurn=false;
					ListenerUtils.releaseListener(member.getId());
					this.sendPublic("已弃权。");
					this.sendPrivate("你已弃权");
					game.NoVote(this);
				}else
				if (content.startsWith("投票")) {
					try {
						Long qq = Long.parseLong(Utils.removeLeadings("投票", content).replace('号', ' ').trim());
						Villager p = game.getPlayerById(qq);
						if (p == null) {
							super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入。");
							return;
						}
						if (p.isDead) {
							super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入。");
							return;
						}
						if(!game.checkCanVote(p)) {
							super.sendPrivate("选择的qq号或者游戏号码非选择对象，请重新输入。");
							return;
						}
						isVoteTurn=false;
						ListenerUtils.releaseListener(super.member.getId());
						game.logger.logSkill(this,p,"投票");
						super.sendPrivate("已投票给 " + p.getMemberString());
						super.sendPublic("已投票给 " + p.getMemberString());
						game.DayVote(this, p);
						
					} catch (Throwable t) {
						super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
					}
				}
			}
		});
	}

	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}

	public void onWolfTurn() {
	};

	public boolean shouldSurvive(DiedReason dir) {
		if(dir==DiedReason.Hunt)
			return isGuarded;
		return isGuarded^isSavedByWitch;
	}
	public boolean onDiePending(DiedReason dir) {
		dr = dir;
		return false;
	};
	public void onSelectSheriff() {
		super.sendPrivate("当前是警长竞选环节，如果要竞选警长，请发送“竞选”，否则请发送“放弃”");
		ListenerUtils.registerListener(super.member,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("竞选")) {
				super.sendPrivate("你参加了竞选。");
				ListenerUtils.releaseListener(super.member.getId());
				game.sherifflist.add(this);
			}else if(content.startsWith("放弃")) {
				super.sendPrivate("你放弃了竞选。");
				ListenerUtils.releaseListener(super.member.getId());
			}
		});
		return;
	}
	public void onSheriffState() {
		sendPublic("你有五分钟时间进行竞选发言。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(member, (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(member.getId());
				game.skipWait(WaitReason.State);
			}
		});
		game.startWait(300000,WaitReason.State);
	};
	public void onSheriffVote() {
		sendPublic("你有五分钟时间进行竞选发言。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(member, (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(member.getId());
				game.skipWait(WaitReason.State);
			}
		});
		game.startWait(300000,WaitReason.State);
	};
	public void onDied(DiedReason dir) {
		dr = dir;
		if (game.isFirstNight || dir.hasDiedWord) {
			isDead = true;
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			if(!this.onDiePending(dir))
				ListenerUtils.registerListener(member, (msg, type) -> {
					if (type == MsgType.AT) {
						ListenerUtils.releaseListener(member.getId());
						game.skipWait(WaitReason.DieWord);
					}
				});
			game.startWait(300000,WaitReason.DieWord);
			sendPublic("说完了。");
			tryMute();
		} else {
			isDead = true;
			sendPublic("死了，没有遗言。");
			tryMute();
		}
	}
	public void StartTurn() {
		isCurrentTurn=true;
	}
	public void EndTurn() {
		isCurrentTurn=false;
	}
	public boolean onReattach(Long m) {
		if(m==mid) {
			member.sendMessage("重置成功!");
			if(this.isCurrentTurn)
				onTurn();
			if(this.isVoteTurn)
				vote();
			return true;
		}
		return false;
	}
	public void onTurn(int turnnumber) {
		if(getTurn()==turnnumber)
			onTurn();
	}
	public void onTurn() {
		
	}
	public String getRole() {
		return "平民";
	}
	public Fraction getFraction() {
		return Fraction.Innocent;
	}
	public double getTicketCount() {
		return isSheriff?1.5:1;
	}
	public String getPredictorRole() {
		return getRole();
	}
	public int getTurn() {
		return 0;
	}
	/**
	 * @param receiver  
	 * @param skid 
	 */
	public void fireSkill(Villager receiver,int skid) {
		
	}
}
