package com.khjxiaogu.TableGames.werewolf;

import java.lang.Thread.State;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

public class WerewolfGame extends Game {
	public enum DiedReason{
		Vote("被驱逐"),
		Wolf("被杀死"),
		Poison("被毒死"),
		Hunter("被射死"),
		Knight("被单挑死"),
		Explode("自爆死"),
		Knight_s("以死谢罪"),
		Hunt("被猎杀"),
		Hunt_s("猎杀失败");
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
			return dr.toString();
		}
	}
	public enum WaitReason{
		Generic(0),
		DieWord(1),
		State(2),
		Vote(3),
		Other(4);
		private final int id;
		private WaitReason(int id) {
			this.id=id;
		}
		public int getId() {
			return id;
		}
	}
	List<Villager> sherifflist=Collections.synchronizedList(new ArrayList<>());
	List<Villager> playerlist=Collections.synchronizedList(new ArrayList<>());
	List<Villager> canVote=null;
	Map<Villager,DiedReason> tokill=new ConcurrentHashMap<>();
	private static Map<String,Class<? extends Villager>> caraMap=new HashMap<>();
	static {
		caraMap.put("乌鸦",Crow.class);
		caraMap.put("石像鬼",Demon.class);
		caraMap.put("守墓人",GraveKeeper.class);
		caraMap.put("守卫",Defender.class);
		caraMap.put("猎人",Hunter.class);
		caraMap.put("白痴",Idiot.class);
		caraMap.put("平民",Villager.class);
		caraMap.put("骑士",Knight.class);
		caraMap.put("长老",Elder.class);
		caraMap.put("预言家",Seer.class);
		caraMap.put("老流氓",Tramp.class);
		caraMap.put("白狼王",WhiteWolf.class);
		caraMap.put("女巫",Witch.class);
		caraMap.put("狼人",Werewolf.class);
		caraMap.put("隐狼",HiddenWolf.class);
		caraMap.put("猎魔人",WolfKiller.class);
	}
	boolean isFirstNight=true;
	boolean canDayVote=false;
	boolean isEnded=false;
	boolean sameTurn=false;
	Villager cursed=null;
	Villager lastCursed=null;
	Object waitLock=new Object();
	VoteUtil<Villager> vu=new VoteUtil<>();
	int num=0;
	Thread[] wt=new Thread[5];
	public List<Class<? extends Villager>> roles=Collections.synchronizedList(new ArrayList<>());
	public WerewolfGame(Group g,int cplayer) {
		super(g,cplayer,8);
		int godcount=(int) Math.ceil(cplayer/3.0);
		if(godcount>5)godcount=5;
		int wolfcount=(int)Math.ceil((cplayer-godcount)/2.0);
		int innocount=cplayer-godcount-wolfcount;
		if(innocount<wolfcount) {
			--innocount;
			roles.add(Elder.class);
		}
		if(cplayer>=8) {
			List<Class<? extends Villager>> exroles=new ArrayList<>();
			exroles.add(Demon.class);
			exroles.add(WhiteWolf.class);
			exroles.add(HiddenWolf.class);
			Collections.shuffle(exroles);
			--wolfcount;
			roles.add(exroles.remove(0));
			if(cplayer>=17) {
				roles.add(exroles.remove(0));
			}
		}
		while(--wolfcount>=0)
			roles.add(Werewolf.class);
		if(innocount>=3) {
			roles.add(Tramp.class);
			--innocount;
		}
		while(--innocount>=0)
			roles.add(Villager.class);
		Collections.shuffle(roles);
		List<Class<? extends Villager>> exroles=new ArrayList<>();
		exroles.add(Defender.class);
		exroles.add(Hunter.class);
		exroles.add(Idiot.class);
		exroles.add(Seer.class);
		exroles.add(Witch.class);
		exroles.add(Crow.class);
		exroles.add(Knight.class);
		exroles.add(GraveKeeper.class);
		exroles.add(WolfKiller.class);
		
		Collections.shuffle(exroles);
		while(--godcount>=0)
			roles.add(exroles.remove(0));
		Collections.shuffle(roles);
		Collections.shuffle(roles);
	}
	public WerewolfGame(Group g,String... args) {
		super(g,args.length,8);
		for(String s:args) {
				roles.add(caraMap.getOrDefault(s,Villager.class));
		}
		Collections.shuffle(roles);
	}
	//game control
	@Override
	protected void doFinalize() {
		vu.clear();
		for(Villager p:playerlist) {
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
		for(Villager in:playerlist) {
			if(in.onReattach(c))
				return true;
		}
		return false;
	}
	public String getAliveList(){
		StringBuilder sb=new StringBuilder("存活：\n");
		for(Villager p:playerlist) {
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
				Villager cp;
				playerlist.add(cp=roles.remove(0).getConstructor(WerewolfGame.class,Member.class).newInstance(this,mem));
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
	public void startWait(long millis,WaitReason lr) {
		try {
			synchronized(waitLock){
				wt[lr.getId()]=Thread.currentThread();
			}
			try {
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
			synchronized(waitLock){
				wt[lr.getId()]=null;
			}
		}catch(Throwable T) {}finally {
			wt[lr.getId()]=null;
		}
	}
	public void skipWait(WaitReason lr) {
		synchronized(waitLock){
			if(wt[lr.getId()]!=null&&wt[lr.getId()].getState()==State.TIMED_WAITING)
				wt[lr.getId()].interrupt();
			wt[lr.getId()]=null;
		}
	}
	public void endWait(WaitReason lr) throws InterruptedException{
		synchronized(waitLock){
			wt[lr.getId()]=null;
		}
	}
	//game logic
	void removeAllListeners() {
		for(Villager p:playerlist) {
			p.EndTurn();
			Utils.releaseListener(p.member.getId());
		}
	}
	public Villager getPlayerById(long id) {
		int i=0;
		for(Villager p:playerlist) {
			if(p.member.getId()==id||i==id)
				return p;
			i++;
		}
		return null;
	}

	public void WolfVote(Villager src,Villager id) {
		if(vu.vote(src,id))
			skipWait(WaitReason.Vote);
	}
	public void DayVote(Villager src,Villager id) {
		if(canDayVote)
			if(vu.vote(src,id))
				skipWait(WaitReason.Vote);
	}
	public boolean checkCanVote(Villager id) {
		if(canVote==null)
			return true;
		return canVote.contains(id);
	}
	public void NoVote(Villager src) {
		vu.giveUp(src);
		if(vu.finished()) 
			skipWait(WaitReason.Vote);
	}
	void kill(Villager p,DiedReason r) {
		if(!tokill.containsKey(p)) {
			tokill.put(p,r);
		}
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
		for(Villager p:playerlist) {
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
	/*public void onUpperNightTurn() {
		this.sendPublicMessage("天黑了，所有人闭眼，有上半夜技能的玩家请睁眼，请私聊决定技能……");
		for(Innocent p2:playerlist) {
			if(p2.isDead)continue;
			p2.onTurn(4);
		}
		startWait(30000);
		removeAllListeners();
		scheduler.execute(()->onWolfTurn());
	}*/
	public void onWolfTurn() {
		sendPublicMessage("天黑了，所有人闭眼，狼人请睁眼，请私聊投票选择你们要杀的人。");
		muteAll(true);
		for(Villager p:playerlist) {
			if(p.isDead)continue;
			p.onWolfTurn();
		}
		startWait(120000,WaitReason.Vote);
		removeAllListeners();
		List<Villager> il=vu.getForceMostVoted();
		if(il.size()>0)
			wolfKill(il.get(0));
		else {
			Villager rd;
			do {
				rd=playerlist.get((int)(Math.random()*playerlist.size()));
			}while(rd.isDead||rd.getFraction()==Fraction.Wolf);
			wolfKill(rd);
		}
	}
	public void wolfKill(Villager p) {
		vu.clear();
		if(p instanceof Elder&&!((Elder) p).lifeUsed) {
			((Elder) p).lifeUsed=true;
		}else if(p!=null)
			tokill.put(p,DiedReason.Wolf);
		this.sendPublicMessage("狼人请闭眼，有夜间技能的玩家请睁眼，请私聊选择你们的技能。");
		for(Villager p2:playerlist) {
			if(p2.isDead)continue;
			p2.onTurn(2);
		}
		startWait(60000,WaitReason.Generic);
		removeAllListeners();
		scheduler.execute(()->onDiePending());
	}
	public void onDiePending() {
		this.sendPublicMessage("有夜间技能的玩家请闭眼，猎人请睁眼，你的开枪状态是……");
		tokill.entrySet().removeIf(in->in.getKey().shouldSurvive(in.getValue()));
		Set<Entry<Villager, DiedReason>> rs=new HashSet<>(tokill.entrySet());
		for(Entry<Villager, DiedReason> p:rs) {
			p.getKey().onDiePending(p.getValue());
		}
		startWait(30000,WaitReason.Generic);
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
		skipWait(WaitReason.State);
		skipWait(WaitReason.Vote);
		skipWait(WaitReason.DieWord);
		skipWait(WaitReason.Generic);
	}
	public void onDayTime() {
		lastVoteOut=null;
		muteAll(false);
		if(!tokill.isEmpty()) {
			StringBuilder sb=new StringBuilder("天亮了，昨晚的死者是：\n");
			for(Villager p:tokill.keySet()) {
				p.isDead=true;
				sb.append(p.getMemberString());
				sb.append("\n");
			}
			if(VictoryPending())return;
			this.sendPublicMessage(sb.toString());
			for(Entry<Villager, DiedReason> p:tokill.entrySet()) {
				p.getKey().onDied(p.getValue());
			}
		}else
			this.sendPublicMessage("昨夜无死者。");
		this.sendPublicMessage(getAliveList());
		tokill.clear();
		this.isFirstNight=false;
		for(Villager p:playerlist) {
			if(!p.isDead) {
				p.onTurnStart();
			}
		}
		isDayTime=true;
		for(Villager p:playerlist) {
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
		this.sendPublicMessage("你们有15秒思考时间，15秒后开始投票。");
		startWait(15000,WaitReason.Generic);
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
		this.sendPublicMessage("请在两分钟内在私聊中完成投票！");
		for(Villager p:playerlist) {
			if(!p.isDead) {
				p.vote();
			}
		}
		if(cursed!=null)
			vu.vote(cursed);
		vu.hintVote(scheduler);
		canDayVote=true;
		startWait(120000,WaitReason.Vote);
		removeAllListeners();
		if(cursed!=null)
			this.sendPublicMessage(cursed.getMemberString()+"被乌鸦诅咒了。");
		voteKill(vu.getMostVoted());
	};
	Villager lastVoteOut;
	public void voteKill(List<Villager> ps) {
		vu.clear();
		if(ps.size()>1) {
			if(!sameTurn) {
				sameTurn=true;
				this.sendPublicMessage("同票，请做最终陈述。");
				MessageChainBuilder mcb=new MessageChainBuilder();
				mcb.append("开始投票，请在两分钟内投给以下人物其中之一：\n");
				muteAll(false);
				canVote=ps;
				for(Villager p:ps) {
					mcb.add(p.getAt());
					mcb.add("\n");
					p.onDayTime();
				}
				mcb.add("请在两分钟内在私聊中完成投票！");
				this.sendPublicMessage(mcb.asMessageChain());
				muteAll(true);
				for(Villager p:playerlist) {
					if(!p.isDead) {
						p.vote();
					}
				}
				if(cursed!=null)
					vu.vote(cursed);
				vu.hintVote(scheduler);
				scheduler.execute(()->{
					startWait(120000,WaitReason.Vote);
					removeAllListeners();
					voteKill(vu.getMostVoted());
				});
				return;
			}
			this.sendPublicMessage("再次同票，跳过回合。");
			ps.clear();
		}
		canVote=null;
		sameTurn=false;
		if(ps.size()==0)
			this.sendPublicMessage("无人出局");
		else {
			Villager p=ps.get(0);
			lastVoteOut=p;
			this.kill(p, DiedReason.Vote);
			muteAll(false);
			for(Entry<Villager, DiedReason> pe:tokill.entrySet()) {
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
		if(!canDayVote&&cursed!=null)
			innos++;
		for(Villager p:playerlist) {
			if(p.isDead)continue;
			total++;
			if(p.getFraction()==Fraction.Wolf) {
				wolfs++;
				continue;
			}
			innos++;
			if(p instanceof Hunter)innos++;
			else if(p instanceof Witch&&((Witch) p).hasPoison)innos++;
			else if(p instanceof Knight&&((Knight) p).hasSkill)innos++;
			else if(p instanceof Idiot&&!((Idiot) p).canVote)innos--;
			else if(p instanceof WolfKiller)innos++;
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
			for(Villager p:playerlist) {
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
