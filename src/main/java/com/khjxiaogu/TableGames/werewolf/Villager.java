package com.khjxiaogu.TableGames.werewolf;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.WaitReason;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.MessageChain;

public class Villager extends com.khjxiaogu.TableGames.Player implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -6011026557044847752L;
	transient WerewolfGame game;
	boolean isDead = false;
	boolean isGuarded = false;
	boolean lastIsGuarded = false;
	boolean showRoleWhenDie = false;
	boolean isCurrentTurn = false;
	boolean isVoteTurn = false;
	boolean isSavedByWitch = false;
	boolean isDemonChecked = false;
	boolean isSheriff = false;
	boolean isExchanged = false;
	boolean isBurned = false;
	boolean isArcherProtected = false;
	boolean isMuted=false;
	boolean lastIsMuted=false;
	boolean isFrozen=false;
	Set<DiedReason> diedReasonStack = new HashSet<>();
	double voteAccuracy;
	int voted;
	double skillAccuracy;
	int skilled;
	// DiedReason dr = null;
	transient Villager prev;
	transient Villager next;
	transient Behaviour vote;
	transient Behaviour desc;
	transient Behaviour dead;
	transient Behaviour[] skills = new Behaviour[4];

	public Villager(WerewolfGame werewolfGame, Member member) {
		super(member);
		game = werewolfGame;
	}

	public Villager(WerewolfGame game, AbstractPlayer p) {
		super(p);
		this.game = game;
		super.bind(this);
	}

	public void onGameStart() {
		sendPrivate("您的身份是：" + getRole() + "。");
		sendPrivate(getJobDescription());
	}

	public String getJobDescription() {
		return "你白天可以进行陈述和投票，目标是与神合作去除狼人。";
	}

	public void doTakeOver(AbstractPlayer ap) {
		member = ap;
		super.bind(this);
		onGameStart();
	}
	public void retake() {
		super.bind(this);
		super.setGame(game);
		onGameStart();
	}
	public void onTurnStart() {
		isSavedByWitch = false;
		lastIsGuarded = false;
		isArcherProtected = false;
		if (isGuarded) {
			lastIsGuarded = true;
			isGuarded = false;
		}
		addDaySkillListener();
	}

	public void addDaySkillListener() {
	}

	public void onDayTime() {
		onBeforeTalk();
		sendPublic("你有五分钟时间进行陈述。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(getId());
				addDaySkillListener();
				game.skipWait(WaitReason.State);
			} else if (type == MsgType.PRIVATE) {
				doDaySkillPending(Utils.getPlainText(msg));
			}
		});
		game.startWait(300000, WaitReason.State);
		onFinishTalk();
	};

	/**
	 * @param plainText
	 */
	public void doDaySkillPending(String plainText) {
	}

	public void onBeforeTalk() {
		tryUnmute();
	}

	public void onFinishTalk() {
		tryMute();
	}
	public void vote() {
		isVoteTurn = true;
		sendPrivate(game.getAliveList());
		super.sendPrivate("请私聊投票要驱逐的人，你有2分钟的考虑时间\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n弃票请输入“弃权”");
		game.vu.addToVote(this);
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					isVoteTurn = false;
					ListenerUtils.releaseListener(getId());
					this.sendPublic("已弃权。");
					sendPrivate("你已弃权");
					game.NoVote(this);
				} else if (content.startsWith("投票")) {
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
						if (!game.checkCanVote(p)) {
							super.sendPrivate("选择的qq号或者游戏号码非选择对象，请重新输入。");
							return;
						}
						isVoteTurn = false;
						ListenerUtils.releaseListener(super.getId());
						increaseVoteAccuracy(p.onVotedAccuracy());
						game.logger.logSkill(this, p, "投票");
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
	public boolean canWolfTurn() {
		return false;
	}
	public void onWolfTurn() {
	};

	public boolean shouldSurvive(DiedReason dir) {
		if (dir == DiedReason.Hunt)
			return isGuarded;
		if (dir == DiedReason.Wolf)
			return isArcherProtected || isGuarded ^ isSavedByWitch;
		return false;
	}

	/**
	 * @param dir  
	 */
	public void onDieSkill(DiedReason dir) {

		// dr = dir;
	};
	@FunctionalInterface
	public interface DoSelect{
		public boolean select(List<Villager> canTalk,Villager lastDeath,Villager sheriff,List<Villager> all);
	}
	public static Map<String,DoSelect> orders=new HashMap<>();
	static {
		orders.put("警后", (l,a,b,c)->{
			Villager cur=b.next;
			while(b!=cur) {
				l.add(cur);
				cur=cur.next;
			}
			l.add(b);
			return true;
		});
		orders.put("警前", (l,a,b,c)->{
			Villager cur=b.prev;
			while(b!=cur) {
				l.add(cur);
				cur=cur.prev;
			}
			l.add(b);
			return true;
		});
		orders.put("死后", (l,a,b,c)->{
			if(a==null)return false;
			Villager cur=a.next;
			while(a!=cur) {
				if(cur!=b)
					l.add(cur);
				cur=cur.next;
			}
			l.add(b);
			return true;
		});
		orders.put("死前", (l,a,b,c)->{
			if(a==null)return false;
			Villager cur=a.prev;
			while(a!=cur) {
				if(cur!=b)
					l.add(cur);
				cur=cur.prev;
			}
			l.add(b);
			return true;
		});
		orders.put("顺序", (l,a,b,c)->{
			Villager ft=c.get(0);
			Villager cur=ft;
			do{
				if(cur!=b)
					l.add(cur);
				cur=cur.next;
			}while(ft!=cur);
			l.add(b);
			return true;
		});
		orders.put("倒序", (l,a,b,c)->{
			Villager ft=c.get(c.size()-1);
			Villager cur=ft;
			do{
				if(cur!=b)
					l.add(cur);
				cur=cur.prev;
			}while(ft!=cur);
			l.add(b);
			return true;
		});
	}
	public boolean onSelectOrder(Villager lastDeath) {
		if(!isSheriff)return false;
		this.sendPublic("请选择发言顺序。");
		this.sendPrivate("请回复选择发言顺序：警后、警前、死后、死前、顺序、倒序。\n" + 
				"你有30秒时间选择。");
		game.canTalk.clear();
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				DoSelect sel=orders.get(content);
				if(sel==null) {
					this.sendPrivate("发言顺序不存在，请重新输入！");
					return;
				}
				if(sel.select(game.canTalk, lastDeath,this,game.playerlist))
					this.sendPrivate("选择成功！");
				else {
					this.sendPrivate("发言顺序不可用，请重新输入！");
					return;
				}
				game.sendPublicMessage("当前发言顺序："+content);
				game.skipWait(WaitReason.Generic);
			}
		});
		game.startWait(30000,WaitReason.Generic);
		ListenerUtils.releaseListener(getId());
		if(game.canTalk.isEmpty()) {
			orders.get("顺序").select(game.canTalk, lastDeath,this,game.playerlist);
		}
		return true;
	}
	public void onSheriffSkill() {
		if(isSheriff) {
			this.sendPublic("请警长选择警徽的处置方式。");
			this.sendPrivate("警长，你死了，你有1分钟时间决定警徽去向，如果要传给某人，可以使用：“传给 qq号或者游戏号码”给该玩家警徽。\n" + 
					"否则，可以使用“撕毁”撕毁警徽。\n");
			ListenerUtils.registerListener(super.getId(), (msg, type) -> onSheriffSkillListener(msg,type));
			game.startWait(60000,WaitReason.Generic);
		}
	}
	public void onSheriffSkillListener(MessageChain msg,MsgType type) {
		if(isSheriff) {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("传给")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("传给", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入。");
						return;
					}
					if (p.isDead) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入。");
						return;
					}
					this.isSheriff=false;
					increaseVoteAccuracy(-p.onVotedAccuracy());
					game.logger.logSkill(this, p, "警徽给");
					super.sendPrivate("已传 " + p.getMemberString());
					super.sendPublic("已把警徽给 " + p.getMemberString());
					p.isSheriff=true;
					game.skipWait(WaitReason.Generic);
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“传给 qq号或者游戏号码”！");
				}
			} else if (content.startsWith("撕毁")) {
				game.logger.logRaw(this.getNameCard()+"撕毁了警徽");
				super.sendPrivate("你撕毁了警徽。");
				super.sendPublic("撕毁了警徽");
				this.isSheriff=false;
				game.skipWait(WaitReason.Generic);
			}
		}
	}
	public boolean onDiePending(DiedReason dir) {
		if (canDeathSkill(dir)) {
			onDieSkill(dir);
			return shouldWaitDeathSkill();
		}
		return false;
	}

	public void onSelectSheriff() {
		super.sendPrivate("当前是警长竞选环节，如果要竞选警长，请在60秒内发送“竞选”，否则请发送“放弃”");
		ListenerUtils.registerListener(super.getId(), (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("竞选")) {
				super.sendPrivate("你参加了竞选。");
				ListenerUtils.releaseListener(super.getId());
				game.sherifflist.add(this);
			} else if (content.startsWith("放弃")) {
				super.sendPrivate("你放弃了竞选。");
				ListenerUtils.releaseListener(super.getId());
			}
		});
		return;
	}
	public void onBeforeSheriffState() {
		sendPrivate("在竞选投票开始前，你随时都可以私聊发送“退选”进行退选。");
		ListenerUtils.registerListener(getId(), (msgx, typex) ->SheriffDeselect(msgx,typex));
	}
	public void onSheriffState() {
		this.onBeforeTalk();
		sendPublic("你有五分钟时间进行竞选发言。\n可以随时@我结束你的讲话。");
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.AT) {
				ListenerUtils.releaseListener(getId());
				ListenerUtils.registerListener(getId(), (msgx, typex) ->SheriffDeselect(msgx,typex));
				game.skipWait(WaitReason.State);
			}else if(type==MsgType.PRIVATE&&Utils.getPlainText(msg).startsWith("退选")) {
				game.logger.logRaw(this.getNameCard()+"已退选");
				this.sendPublic("已退选");
				game.sherifflist.remove(this);
				game.skipWait(WaitReason.State);
			}
		});
		game.startWait(300000, WaitReason.State);
		this.onFinishTalk();
	};
	public void SheriffDeselect(MessageChain msg, MsgType type) {
		if(type==MsgType.PRIVATE&&Utils.getPlainText(msg).startsWith("退选")) {
			game.logger.logRaw(this.getNameCard()+"已退选");
			this.sendPublic("已退选");
			game.sherifflist.remove(this);
		}
	}
	public void onSheriffVote() {
		StringBuilder sb = new StringBuilder("警长竞选列表：\n");
		for (Villager p : game.sherifflist) {
			sb.append(p.getMemberString());
			sb.append("\n");
		}
		sendPrivate(sb.toString());
		super.sendPrivate("请私聊投票选择的警长，你有2分钟的考虑时间\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n弃票请输入“弃权”");
		game.vu.addToVote(this);
		ListenerUtils.registerListener(getId(), (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					ListenerUtils.releaseListener(getId());
					this.sendPublic("已弃权。");
					sendPrivate("你已弃权");
					game.NoVote(this);
				} else if (content.startsWith("投票")) {
					try {
						Long qq = Long.parseLong(Utils.removeLeadings("投票", content).replace('号', ' ').trim());
						Villager p = game.getPlayerById(qq);
						if (p == null) {
							super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入。");
							return;
						}
						if (!game.sherifflist.contains(p)) {
							super.sendPrivate("选择的qq号或者游戏号码非选择对象，请重新输入。");
							return;
						}
						ListenerUtils.releaseListener(super.getId());
						increaseVoteAccuracy(-p.onVotedAccuracy());
						game.logger.logSkill(this, p, "投票");
						super.sendPrivate("已投票给 " + p.getMemberString());
						super.sendPublic("已投票给 " + p.getMemberString());
						game.SheriffVote(this, p);
					} catch (Throwable t) {
						super.sendPrivate("发生错误，正确格式为：“投票 qq号或者游戏号码”！");
					}
				}
			}
		});
	};

	public void onDied(DiedReason dir) {
		this.populateDiedReason(dir);
		onDied(dir, true);
	}

	public void onDied(DiedReason dir, boolean shouldCheckSkill) {
		// dr = dir;
		if(shouldCheckSkill)
			onSheriffSkill();
		if (game.isFirstNight || dir.hasDiedWord) {
			isDead = true;
			onBeforeTalk();
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			if (!shouldCheckSkill || !onDiePending(dir)) {
				ListenerUtils.registerListener(getId(), (msg, type) -> {
					if (type == MsgType.AT) {
						ListenerUtils.releaseListener(getId());
						game.skipWait(WaitReason.DieWord);
					}
				});
			}
			game.startWait(300000, WaitReason.DieWord);
			sendPublic("说完了。");
			tryMute();
		} else {
			isDead = true;
			sendPublic("死了，没有遗言。");
			tryMute();
		}
	}

	public boolean shouldWaitDeathSkill() {
		return isSheriff;
	}

	/**
	 * @param dir
	 */
	public boolean canDeathSkill(DiedReason dir) {
		return isSheriff;
	}

	public void StartTurn() {
		isCurrentTurn = true;
	}

	public void EndTurn() {
		isCurrentTurn = false;
	}

	public boolean onReattach(Long m) {
		if (m == getId()) {
			sendPrivate("重置成功!");
			if (isCurrentTurn) {
				onTurn();
			}
			if (isVoteTurn) {
				vote();
			}
			return true;
		}
		return false;
	}

	public void onTurn(int turnnumber) {
		if (getTurn() == turnnumber) {
			onTurn();
		}
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
		return isSheriff ? 1.5 : 1;
	}

	public String getPredictorRole() {
		return getRole();
	}

	public double onVotedAccuracy() {
		return -0.25;
	}

	public double onSkilledAccuracy() {
		return -0.25;
	}

	public boolean shouldReplace(DiedReason src, DiedReason dest) {
		return src.canBeReplaced(dest);
	}

	public void increaseVoteAccuracy(double val) {
		voteAccuracy += val;
		voted += 1;
	}

	public void increaseSkilledAccuracy(double val) {
		voteAccuracy += val;
		skilled += 1;
	}

	public boolean shouldSurvive() {
		diedReasonStack.removeIf((dr) -> this.shouldSurvive(dr));
		return diedReasonStack.isEmpty();
	}

	public boolean hasDiedReason(DiedReason dir) {
		return diedReasonStack.contains(dir);
	}

	public void populateDiedReason(DiedReason dir) {
		diedReasonStack.add(dir);
	}

	public DiedReason getEffectiveDiedReason() {
		if (diedReasonStack.isEmpty())
			return null;
		DiedReason dor = diedReasonStack.iterator().next();
		for (DiedReason dr : diedReasonStack) {
			if (dor.canBeReplaced(dr)) {
				dor = dr;
			}
		}
		return dor;
	}

	public int getTurn() {
		return 0;
	}

	/**
	 * @param receiver
	 * @param skid
	 */
	public void fireSkill(Villager receiver, int skid) {

	}
}
