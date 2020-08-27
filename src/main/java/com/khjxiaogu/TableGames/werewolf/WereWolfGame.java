package com.khjxiaogu.TableGames.werewolf;

import java.lang.Thread.State;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.VoteUtil;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class WereWolfGame extends Game {
	public enum DiedReason{
		Vote("驱逐"),
		Wolf("杀死"),
		Poison("毒死"),
		Hunter("射死"),
		Knight("单挑死"),
		Explode("自爆死"),
		Knight_s("以死谢罪");
		String desc;

		private DiedReason(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return desc;
		}
		public static String getString(DiedReason dr) {
			if(dr==null)
				return "存活";
			return "被"+dr.toString();
		}
	}
	List<Innocent> playerlist=Collections.synchronizedList(new ArrayList<>());
	Map<Innocent,DiedReason> tokill=new ConcurrentHashMap<>();
	boolean isFirstNight=true;
	boolean canDayVote=false;
	boolean isEnded=false;
	boolean sameTurn=false;
	Innocent cursed=null;
	Innocent lastCursed=null;
	Object waitLock=new Object();
	VoteUtil<Innocent> vu=new VoteUtil<>();
	int num=0;
	Thread wt=null;
	public List<Class<? extends Innocent>> roles=Collections.synchronizedList(new ArrayList<>());
	public WereWolfGame(Group g,int cplayer) {
		super(g,cplayer,8);
		int godcount=(int) Math.ceil(cplayer/3.0);
		if(godcount>5)godcount=5;
		int wolfcount=(int)Math.ceil((cplayer-godcount)/2.0);
		int innocount=cplayer-godcount-wolfcount;
		if(innocount<wolfcount) {
			--innocount;
			roles.add(OldMan.class);
		}
		while(--wolfcount>=0)
			roles.add(Wolf.class);
		while(--innocount>=0)
			roles.add(Innocent.class);
		Collections.shuffle(roles);
		List<Class<? extends Innocent>> exroles=new ArrayList<>();
		exroles.add(Guard.class);
		exroles.add(Hunter.class);
		exroles.add(Idoit.class);
		exroles.add(Predictor.class);
		exroles.add(Witch.class);
		exroles.add(Crow.class);
		exroles.add(Knight.class);
		Collections.shuffle(exroles);
		while(--godcount>=0)
			roles.add(exroles.remove(0));
		Collections.shuffle(roles);
		Collections.shuffle(roles);
	}
	@SuppressWarnings("unchecked")
	public WereWolfGame(Group g,String... args) {
		super(g,args.length,8);
		for(String s:args) {
			try {
				roles.add((Class<? extends Innocent>) Class.forName("com.khjxiaogu.TableGames.werewolf."+s));
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Collections.shuffle(roles);
	}
	//game control
	@Override
	protected void doFinalize() {
		vu.clear();
		for(Innocent p:playerlist) {
			Utils.releaseListener(p.member.getId());
			Utils.RemoveMember(p.member.getId());
		}
		super.doFinalize();
	}
	@Override
	public String getName() {
		return "狼人杀";
	}
	@Override
	public boolean isAlive() {
		return !isEnded;
	}
	@Override
	public boolean onReAttach(Long c) {
		for(Innocent in:playerlist) {
			if(in.onReattach(c))
				return true;
		}
		return false;
	}
	public String getAliveList(){
		StringBuilder sb=new StringBuilder("存活：\n");
		for(Innocent p:playerlist) {
			if(!p.isDead) {
				sb.append(p.getMemberString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	@Override
	public boolean addMember(Member mem) {
		if(this.getPlayerById(mem.getId())!=null) {
			this.sendPublicMessage(new At(mem).plus("你已经报名了！"));
			return false;
		}
		if(!Utils.tryAddMember(mem.getId())) {
			this.sendPublicMessage(new At(mem).plus("你已参加其他游戏！"));
			return true;
		}
		if(roles.size()>0) {
			try {
				Innocent cp;
				playerlist.add(cp=roles.remove(0).getConstructor(WereWolfGame.class,Member.class).newInstance(this,mem));
				int min=playerlist.indexOf(cp);
				cp.sendPrivate("已经报名");
				String nc=cp.member.getNameCard();
				if(nc.indexOf('|')!=-1) {
					nc=nc.split("\\|")[1];
				}
				cp.member.setNameCard(min+"号 |"+nc);
				if(roles.size()==0) {
					this.sendPublicMessage("狼人杀已满人，游戏即将开始。");
					scheduler.execute(()->gameStart());
				}
				return true;
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	@Override
	public void forceStart() {
		roles.clear();
		scheduler.execute(()->gameStart());
	}
	//wait utils
	public void startWait(long millis) {
		try {
			synchronized(waitLock){
				wt=Thread.currentThread();
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
			synchronized(waitLock){
				wt=null;
			}
		}catch(Throwable T) {}finally {
			wt=null;
		}
	}
	public void skipWait() {
		synchronized(waitLock){
			if(wt!=null&&wt.getState()==State.TIMED_WAITING)
				wt.interrupt();
			wt=null;
		}
	}
	public void endWait() throws InterruptedException{
		synchronized(waitLock){
			wt=null;
		}
	}
	//game logic
	void removeAllListeners() {
		for(Innocent p:playerlist) {
			p.EndTurn();
			Utils.releaseListener(p.member.getId());
		}
	}
	public Innocent getPlayerById(long id) {
		int i=0;
		for(Innocent p:playerlist) {
			if(p.member.getId()==id||i==id)
				return p;
			i++;
		}
		return null;
	}

	public void WolfVote(Innocent src,Innocent id) {
		if(vu.vote(src,id))
			skipWait();
	}
	public void DayVote(Innocent src,Innocent id) {
		if(canDayVote)
			if(vu.vote(src,id))
				skipWait();
	}
	public void NoVote(Innocent src) {
		vu.giveUp(src);
		if(vu.finished()) 
			skipWait();
	}
	void kill(Innocent p,DiedReason r) {
		tokill.put(p,r);
	}
	/**
	 * @param isMute  
	 */
	private void muteAll(boolean isMute) {
		group.getSettings().setMuteAll(isMute);
	}
	//开始游戏流程
	public void gameStart() {
		muteAll(true);
		StringBuilder sb=new StringBuilder("玩家列表：\n");
		for(Innocent p:playerlist) {
			p.onGameStart();
			sb.append(p.getMemberString());
			sb.append("\n");
		}
		this.sendPublicMessage(sb.toString());
		onDawn();
	}
	//混合循环
	public void onDawn() {
		lastCursed=cursed;
		cursed=null;
		isDayTime=false;
		vu.clear();
		removeAllListeners();
		onWolfTurn();
	}
	public void onWolfTurn() {
		sendPublicMessage("天黑了，所有人闭眼，狼人请睁眼，请私聊投票选择你们要杀的人。");
		muteAll(true);
		for(Innocent p:playerlist) {
			if(p.isDead)continue;
			p.onWolfTurn();
		}
		startWait(120000);
		removeAllListeners();
		wolfKill(vu.getForceMostVoted().get(0));
	}
	public void wolfKill(Innocent p) {
		vu.clear();
		if(p instanceof OldMan&&!((OldMan) p).lifeUsed) {
			((OldMan) p).lifeUsed=true;
		}else if(p!=null)
			tokill.put(p,DiedReason.Wolf);
		this.sendPublicMessage("狼人请闭眼，有夜间技能的玩家请睁眼，请私聊选择你们的技能。");
		for(Innocent p2:playerlist) {
			if(p2.isDead)continue;
			p2.onTurn(2);
		}
		startWait(60000);
		removeAllListeners();
		scheduler.execute(()->onDiePending());
	}
	public void onDiePending() {
		this.sendPublicMessage("有夜间技能的玩家请闭眼，猎人请睁眼，你的开枪状态是……");
		tokill.entrySet().removeIf(in->{
			Innocent ip=in.getKey();
			return ip.isGuarded^ip.isSavedByWitch;
		});
		Set<Entry<Innocent, DiedReason>> rs=new HashSet<>(tokill.entrySet());
		for(Entry<Innocent, DiedReason> p:rs) {
			p.getKey().onDiePending(p.getValue());
		}
		startWait(30000);
		removeAllListeners();
		scheduler.execute(()->onDayTime());
	}
	Boolean isDaySkipped=false;
	boolean isDayTime=false;
	public void skipDay() {
		synchronized(isDaySkipped) {
			if(isDayTime) {
				isDaySkipped=true;
			}
		}
		skipWait();
	}
	public void onDayTime() {
		muteAll(false);
		if(!tokill.isEmpty()) {
			StringBuilder sb=new StringBuilder("天亮了，昨晚的死者是：\n");
			for(Innocent p:tokill.keySet()) {
				p.isDead=true;
				sb.append(p.getMemberString());
				sb.append("\n");
			}
			if(VictoryPending())return;
			this.sendPublicMessage(sb.toString());
			for(Entry<Innocent, DiedReason> p:tokill.entrySet()) {
				p.getKey().onDied(p.getValue());
			}
		}else
			this.sendPublicMessage("昨夜无死者。");
		this.sendPublicMessage(getAliveList());
		tokill.clear();
		this.isFirstNight=false;
		for(Innocent p:playerlist) {
			if(!p.isDead) {
				p.onTurnStart();
			}
		}
		isDayTime=true;
		for(Innocent p:playerlist) {
			if(!p.isDead) {
				p.onDayTime();
				boolean needSkip=false;
				synchronized(isDaySkipped) {
					if(isDaySkipped) {
						isDaySkipped=false;
						canDayVote=false;
						needSkip=true;
						isDayTime=false;
					}
				}
				if(needSkip) {
					if(VictoryPending())return;
					scheduler.execute(()->onDawn());
					return;
				}
			}
		}
		muteAll(true);
		this.sendPublicMessage("请在两分钟内在私聊中完成投票！");
		for(Innocent p:playerlist) {
			if(!p.isDead) {
				p.vote();
			}
		}
		if(cursed!=null)
			vu.vote(cursed);
		vu.hintVote(scheduler);
		canDayVote=true;
		startWait(120000);
		removeAllListeners();
		if(cursed!=null)
			this.sendPublicMessage(cursed.getMemberString()+"被乌鸦诅咒了。");
		voteKill(vu.getMostVoted());
	};
	public void voteKill(List<Innocent> ps) {
		vu.clear();
		if(ps.size()>1) {
			if(!sameTurn) {
				sameTurn=true;
				this.sendPublicMessage("同票，请做最终陈述。");
				MessageChainBuilder mcb=new MessageChainBuilder();
				mcb.append("开始投票，请在两分钟内投给以下人物其中之一：\n");
				muteAll(false);
				for(Innocent p:ps) {
					mcb.add(p.getAt());
					mcb.add("\n");
					p.onDayTime();
				}
				mcb.add("请在两分钟内在私聊中完成投票！");
				this.sendPublicMessage(mcb.asMessageChain());
				muteAll(true);
				for(Innocent p:playerlist) {
					if(!p.isDead) {
						p.vote();
					}
				}
				if(cursed!=null)
					vu.vote(cursed);
				vu.hintVote(scheduler);
				scheduler.execute(()->{
					startWait(120000);
					removeAllListeners();
					voteKill(vu.getMostVoted());
				});
				return;
			}
			this.sendPublicMessage("再次同票，跳过回合。");
			ps.clear();
		}
		sameTurn=false;
		if(ps.size()==0)
			this.sendPublicMessage("无人出局");
		else {
			Innocent p=ps.get(0);
			this.kill(p, DiedReason.Vote);
			muteAll(false);
			for(Entry<Innocent, DiedReason> pe:tokill.entrySet()) {
				pe.getKey().onDied(pe.getValue());
			}
			if(VictoryPending())return;
		}
		tokill.clear();
		canDayVote=false;
		onDawn();
	}
	//结束回合循环
	public boolean VictoryPending() {
		int total=0;
		int innos=0;
		int wolfs=0;
		for(Innocent p:playerlist) {
			if(p.isDead)continue;
			total++;
			if(p instanceof Wolf) {
				wolfs++;
				continue;
			}
			innos++;
			if(p instanceof Hunter)innos++;
			else if(p instanceof Witch&&((Witch) p).hasPoison)innos++;
			else if(p instanceof Crow)innos++;
			else if(p instanceof Knight&&((Knight) p).hasSkill)innos++;
			else if(p instanceof Idoit&&!((Idoit) p).canVote)innos--;
		}
		boolean ends=false;
		String status=null;
		if(innos==0&&wolfs>0) {
			status=("游戏结束！狼人获胜\n");
			ends=true;
		}else if(wolfs==0&&innos>0) {
			status=("游戏结束！平民获胜\n");
			ends=true;
		}else if(total==0){
			status=("游戏结束！同归于尽\n");
			ends=true;
		}else if((wolfs>=innos)) {
			status=("游戏结束！狼人获胜\n");
			ends=true;
		}
		if(ends) {
			removeAllListeners();
			MessageChainBuilder mc=new MessageChainBuilder();
			mc.add(status);
			mc.add("游戏身份：\n");
			for(Innocent p:playerlist) {
				mc.add(p.getMemberString());
				mc.add("的身份为 "+p.getRole()+" "+DiedReason.getString(p.dr)+"\n");
				String nc=p.member.getNameCard();
				if(nc.indexOf('|')!=-1) {
					nc=nc.split("\\|")[1];
				}
				p.member.setNameCard(nc);
				try {
				if(p.isDead)p.member.unmute();
				}catch(Throwable t) {}
			}
			muteAll(false);
			this.sendPublicMessage(mc.asMessageChain());
			doFinalize();
		}
		isEnded=ends;
		return ends;
	}

}
