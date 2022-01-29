package com.khjxiaogu.TableGames.game.werewolf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.WaitReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.UserFunction;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.utils.Utils;



public class Villager extends UserFunction implements Serializable {
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
	Set<DiedReason> diedReasonStack = Collections.synchronizedSet(new HashSet<>());
	double voteAccuracy;
	int voted;
	@PlayerDefined
	int index;
	double skillAccuracy;
	int skilled;
	// DiedReason dr = null;
	@PlayerDefined
	transient Villager prev;
	@PlayerDefined
	transient Villager next;
	@PlayerDefined
	String origname;
	public Villager(WerewolfGame game, AbstractUser p) {
		super(p);
		this.game = game;
		super.bind(this);
	}

	public void onGameStart() {
		sendPrivate("您的身份是：" + getRole() + "。");
		sendPrivate(getJobDescription());
	}
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		aInputStream.defaultReadObject();
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}
	public String getJobDescription() {
		return "你白天可以进行陈述和投票，目标是与神合作去除狼人。";
	}

	public void doTakeOver(AbstractUser ap) {
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
	public Villager getPrevWolf() {
		Villager firstWolf = prev;
		while (firstWolf != this) {
			if ((!firstWolf.isDead())&&firstWolf instanceof Werewolf) {
				break;
			}
			firstWolf=firstWolf.prev;
		}
		if (firstWolf == this) {
			firstWolf = prev;
			while (firstWolf != this) {
				if ((!firstWolf.isDead())&&firstWolf.getRealFraction() == Fraction.Wolf) {
					break;
				}
				firstWolf=firstWolf.prev;
			}
		}
		if (firstWolf.getRealFraction() == Fraction.Wolf && firstWolf != this) {
			return firstWolf;
		}
		return null;
	}
	public void onDayTime() {
		onBeforeTalk();
		try {
		sendPublic("你有五分钟时间进行陈述。\n可以随时@我结束你的讲话。");
		super.registerListener( (msg, type) -> {
			if (type == MsgType.AT) {
				super.releaseListener();
				addDaySkillListener();
				game.skipWait(WaitReason.State);
			} else if (type == MsgType.PRIVATE) {
				doDaySkillPending(Utils.getPlainText(msg));
			}
		});
		game.startWait(300000, WaitReason.State);
		}finally {
			onFinishTalk();
		}
	}

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
		super.registerListener( (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					isVoteTurn = false;
					super.releaseListener();
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
						if (p.isDead()) {
							super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入。");
							return;
						}
						if (!game.checkCanVote(p)) {
							super.sendPrivate("选择的qq号或者游戏号码非选择对象，请重新输入。");
							return;
						}
						isVoteTurn = false;
						super.releaseListener();
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
	}

	public boolean shouldSurvive(DiedReason dir) {
		if (dir == DiedReason.Hunt||dir==DiedReason.Shoot)
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
	}
	@FunctionalInterface
	public interface DoSelect{
		boolean select(List<Villager> canTalk,Villager lastDeath,Villager sheriff,List<Villager> all);
	}
	public static Map<String,DoSelect> orders=new HashMap<>();
	static {
		Villager.orders.put("警后", (l,a,b,c)->{
			Villager cur=b.next;
			while(b!=cur) {
				l.add(cur);
				cur=cur.next;
			}
			l.add(b);
			return true;
		});
		Villager.orders.put("警前", (l,a,b,c)->{
			Villager cur=b.prev;
			while(b!=cur) {
				l.add(cur);
				cur=cur.prev;
			}
			l.add(b);
			return true;
		});
		Villager.orders.put("死后", (l,a,b,c)->{
			if(a==null)return false;
			Villager cur=a.next;
			while(a!=cur) {
				if(cur!=b) {
					l.add(cur);
				}
				cur=cur.next;
			}
			l.add(b);
			return true;
		});
		Villager.orders.put("死前", (l,a,b,c)->{
			if(a==null)return false;
			Villager cur=a.prev;
			while(a!=cur) {
				if(cur!=b) {
					l.add(cur);
				}
				cur=cur.prev;
			}
			l.add(b);
			return true;
		});
		Villager.orders.put("顺序", (l,a,b,c)->{
			Villager ft=c.get(0);
			Villager cur=ft;
			do{
				if(cur!=b) {
					l.add(cur);
				}
				cur=cur.next;
			}while(ft!=cur);
			l.add(b);
			return true;
		});
		Villager.orders.put("倒序", (l,a,b,c)->{
			Villager ft=c.get(c.size()-1);
			Villager cur=ft;
			do{
				if(cur!=b) {
					l.add(cur);
				}
				cur=cur.prev;
			}while(ft!=cur);
			l.add(b);
			return true;
		});
	}
	public boolean onSelectOrder(Villager lastDeath) {
		if(!isSheriff)return false;
		this.sendPublic("请选择发言顺序。");
		sendPrivate("请回复选择发言顺序：警后、警前、死后、死前、顺序、倒序。\n" +
				"你有30秒时间选择。");
		game.canTalk.clear();
		super.registerListener( (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				DoSelect sel=Villager.orders.get(content);
				if(sel==null) {
					sendPrivate("发言顺序不存在，请重新输入！");
					return;
				}
				if(sel.select(game.canTalk, lastDeath,this,game.playerlist)) {
					sendPrivate("选择成功！");
				} else {
					sendPrivate("发言顺序不可用，请重新输入！");
					return;
				}
				game.sendPublicMessage("当前发言顺序："+content);
				game.skipWait(WaitReason.Generic);
			}
		});
		game.startWait(30000,WaitReason.Generic);
		super.releaseListener();
		if(game.canTalk.isEmpty()) {
			game.sendPublicMessage("当前发言顺序：顺序");
			Villager.orders.get("顺序").select(game.canTalk, lastDeath,this,game.playerlist);
		}
		return true;
	}
	public void onSheriffSkill() {
		if(isSheriff) {
			this.sendPublic("请警长选择警徽的处置方式。");
			sendPrivate("警长，你死了，你有1分钟时间决定警徽去向，如果要传给某人，可以使用：“传给 qq号或者游戏号码”给该玩家警徽。\n" +
					"否则，可以使用“撕毁”撕毁警徽。\n");
			super.registerListener( (msg, type) -> onSheriffSkillListener(msg,type));
			game.startWait(60000,WaitReason.Generic);
			isSheriff=false;
		}
	}
	public void onSheriffSkillListener(IMessageCompound msg,MsgType type) {
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
					if (p.isDead()) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入。");
						return;
					}
					isSheriff=false;
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
				game.logger.logRaw(getNameCard()+"撕毁了警徽");
				super.sendPrivate("你撕毁了警徽。");
				super.sendPublic("撕毁了警徽");
				isSheriff=false;
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
		super.registerListener( (msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("竞选")) {
				super.sendPrivate("你参加了竞选。");
				super.releaseListener();
				game.sherifflist.add(this);
			} else if (content.startsWith("放弃")) {
				super.sendPrivate("你放弃了竞选。");
				super.releaseListener();
			}
		});
		return;
	}
	public void onBeforeSheriffState() {
		sendPrivate("在竞选投票开始前，你随时都可以私聊发送“退选”进行退选。");
		super.registerListener( (msgx, typex) ->SheriffDeselect(msgx,typex));
	}
	public void onSheriffState() {
		onBeforeTalk();
		sendPublic("你有五分钟时间进行竞选发言。\n可以随时@我结束你的讲话。");
		super.registerListener( (msg, type) -> {
			if (type == MsgType.AT) {
				super.releaseListener();
				super.registerListener( (msgx, typex) ->SheriffDeselect(msgx,typex));
				game.skipWait(WaitReason.State);
			}else if(type==MsgType.PRIVATE&&Utils.getPlainText(msg).startsWith("退选")) {
				game.logger.logRaw(getNameCard()+"已退选");
				this.sendPublic("已退选");
				game.sherifflist.remove(this);
				game.skipWait(WaitReason.State);
			}
		});
		game.startWait(300000, WaitReason.State);
		onFinishTalk();
	}
	public void SheriffDeselect(IMessageCompound msg, MsgType type) {
		if(type==MsgType.PRIVATE&&Utils.getPlainText(msg).startsWith("退选")) {
			game.logger.logRaw(getNameCard()+"已退选");
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
		super.registerListener( (msg, type) -> {
			if (type == MsgType.PRIVATE) {
				String content = Utils.getPlainText(msg);
				if (content.startsWith("弃权")) {
					super.releaseListener();
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
						super.releaseListener();
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
	}

	public void onDied(DiedReason dir) {
		populateDiedReason(dir);
		onDied(dir, true);
	}

	public void onDied(DiedReason dir, boolean shouldCheckSkill) {
		// dr = dir;
		if(shouldCheckSkill) {
			onSheriffSkill();
		}
		if (game.isFirstNight() || dir.hasDiedWord) {
			isDead = true;
			onBeforeTalk();
			sendPublic("死了，你有五分钟时间说出你的遗言。\n可以随时@我结束你的讲话。");
			if (!shouldCheckSkill || !onDiePending(dir)) {
				super.registerListener( (msg, type) -> {
					if (type == MsgType.AT) {
						super.releaseListener();
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

	public Fraction getRealFraction() {
		return Fraction.Innocent;
	}
	public Fraction getPredictorFraction() {
		return getRealFraction();
	}
	public double getTicketCount() {
		return isSheriff ? 1.5 : 1;
	}

	public String getPredictorRole() {
		return getRole();
	}

	public double onVotedAccuracy() {
		return 0.5;
	}

	public double onSkilledAccuracy() {
		return 0.5;
	}
	public double onWolfKilledAccuracy() {
		return onSkilledAccuracy();
	}
	public boolean shouldReplace(DiedReason src, DiedReason dest) {
		return src.canBeReplaced(dest);
	}
	static final double one_const=1+1D/3D*0.2;
	public void increaseVoteAccuracy(double val) {
		voteAccuracy += val*(one_const-game.getCSR()*0.2);
		voted += 1;
	}

	public void increaseSkilledAccuracy(double val) {
		voteAccuracy += val*(one_const-game.getCSR()*0.2);
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

	public boolean isDead() {
		return isDead;
	}
}
