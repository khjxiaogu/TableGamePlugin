package com.khjxiaogu.TableGames.werewolf;

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
import com.khjxiaogu.TableGames.TableGames;
import com.khjxiaogu.TableGames.data.PlayerDatabase.GameData;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.ImagePrintStream;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.VoteHelper;
import com.khjxiaogu.TableGames.utils.WaitThread;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Submsgtype0xdd.Submsgtype0xdd.MsgBody.PlayerState;

public class WerewolfGame extends Game {
	
	public enum DiedReason{
		Vote("被驱逐",true,true),
		Wolf("被杀死",true,false),
		Poison("被毒死",false,false),
		Hunter("被射死",false,false),
		DarkWolf("被狼王杀死",true,false),
		Knight("被单挑死",false,false),
		Explode("自爆死",false,true),
		Knight_s("以死谢罪",false,false),
		Hunt("被猎杀",true,false),
		Reflect("被反伤",false,false),
		Love("殉情",false,false),
		Hunt_s("猎杀失败",false,false);
		String desc;
		final boolean canUseSkill;
		final boolean hasDiedWord;

		private DiedReason(String desc, boolean canUseSkill, boolean hasDiedWord) {
			this.desc = desc;
			this.canUseSkill = canUseSkill;
			this.hasDiedWord = hasDiedWord;
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
	@FunctionalInterface
	public interface RoleRoller{
		public List<Class<? extends Villager>> roll(int cplayer);
	}
	List<Villager> sherifflist=Collections.synchronizedList(new ArrayList<>());
	List<Villager> playerlist=Collections.synchronizedList(new ArrayList<>());
	List<Villager> canVote=null;
	double winrate=0;
	private static Map<Class<? extends Villager>,Double> rolePoint=new HashMap<>();
	Map<Villager,DiedReason> tokill=new ConcurrentHashMap<>();
	private static Map<String,Class<? extends Villager>> caraMap=new HashMap<>();
	private static Map<String,RoleRoller> patterns=new HashMap<>();
	WerewolfGameLogger logger=new WerewolfGameLogger();
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
		caraMap.put("恶灵骑士",NightmareKnight.class);
		caraMap.put("狼王",DarkWolf.class);
		caraMap.put("狼美人",WolfBeauty.class);
		caraMap.put("熊",Bear.class);
		rolePoint.put(Crow.class,1D);
		rolePoint.put(Demon.class,-0.5);
		rolePoint.put(GraveKeeper.class,0.5);
		rolePoint.put(Defender.class,1D);
		rolePoint.put(Hunter.class,1D);
		rolePoint.put(Idiot.class,0.5);
		rolePoint.put(Villager.class,0D);
		rolePoint.put(Knight.class,1.5);
		rolePoint.put(Elder.class,0D);
		rolePoint.put(Seer.class,1D);
		rolePoint.put(Tramp.class,0D);
		rolePoint.put(WhiteWolf.class,-1.5);
		rolePoint.put(Witch.class,1D);
		rolePoint.put(Werewolf.class,-1D);
		rolePoint.put(HiddenWolf.class,-1D);
		rolePoint.put(WolfKiller.class,1.5);
		rolePoint.put(NightmareKnight.class,-1.5);
		rolePoint.put(DarkWolf.class,-1.5);
		rolePoint.put(WolfBeauty.class,-1.5);
		rolePoint.put(Bear.class,1D);
		patterns.put("默认",(cp)->fairRollRole(cp));
		patterns.put("随机",(cp)->rollRole(cp));
		patterns.put("诸神",(cn)->godFightRollRole(cn));
		patterns.put("猎人",(cp)->hunterRollRole(cp));
	}
	boolean isFirstNight=true;
	boolean canDayVote=false;
	boolean isEnded=false;
	boolean sameTurn=false;
	boolean canNoKill=false;
	boolean hunterMustShoot=false;
	boolean doStat=true;
	boolean hasTramp=false;
	boolean hasElder=false;
	int day=0;
	Villager cursed=null;
	Villager lastCursed=null;
	Object waitLock=new Object();
	VoteHelper<Villager> vu=new VoteHelper<>();
	int num=0;
	WaitThread[] wt=new WaitThread[5];
	Runnable next;
	public List<Class<? extends Villager>> roles;

	public WerewolfGame(Group g,int cplayer) {
		super(g,cplayer,cplayer*2);
		for(int i=0;i<wt.length;i++) {
			wt[i]=new WaitThread();
		}
		roles=Collections.synchronizedList(fairRollRole(cplayer));
		winrate=calculateRolePoint(roles);
		for(Class<? extends Villager> role:roles) {
			if(Tramp.class.isAssignableFrom(role)) {
				hasTramp=true;
				continue;
			}
			if(Elder.class.isAssignableFrom(role)) {
				hasElder=true;
				continue;
			}
		}
	}
	public WerewolfGame(Group g,String... args) {
		super(g,args.length,args.length*2);
		for(int i=0;i<wt.length;i++) {
			wt[i]=new WaitThread();
		}
		roles=Collections.synchronizedList(new ArrayList<>());
		for(String s:args) {
			roles.add(caraMap.getOrDefault(s,Villager.class));
		}
		Collections.shuffle(roles);
		winrate=calculateRolePoint(roles);
		for(Class<? extends Villager> role:roles) {
			if(Tramp.class.isAssignableFrom(role)) {
				hasTramp=true;
				continue;
			}
			if(Elder.class.isAssignableFrom(role)) {
				hasElder=true;
				continue;
			}
		}
	}
	public WerewolfGame(Group g,int cplayer,Map<String,String> sets) {
		super(g,cplayer,cplayer*2);
		for(int i=0;i<wt.length;i++) {
			wt[i]=new WaitThread();
		}
		roles=Collections.synchronizedList(patterns.getOrDefault(sets.getOrDefault("板","默认"),(cp)->fairRollRole(cp)).roll(cplayer));
		canNoKill=sets.getOrDefault("空刀","false").equals("true");
		hunterMustShoot=sets.getOrDefault("压枪","false").equals("true");
		doStat=sets.getOrDefault("统计","true").equals("true");
		isFirstNight=sets.getOrDefault("首夜发言","true").equals("true");
		Collections.shuffle(roles);
		winrate=calculateRolePoint(roles);
		for(Class<? extends Villager> role:roles) {
			if(Tramp.class.isAssignableFrom(role)) {
				hasTramp=true;
				continue;
			}
			if(Elder.class.isAssignableFrom(role)) {
				hasElder=true;
				continue;
			}
		}
	}
	public String getGameRules() {
		StringBuilder sb=new StringBuilder("游戏规则设定：");
		if(canNoKill)
			sb.append("\n允许狼人空刀");
		else
			sb.append("\n狼人必须杀人");
		if(hasTramp)
			sb.append("\n有老流氓");
		if(hasElder)
			sb.append("\n有长老");
		if(roles.size()+playerlist.size()>=8)
			sb.append("\n允许狼神");
		if(hunterMustShoot)
			sb.append("\n猎人不能压枪");
		if(doStat&&roles.size()+playerlist.size()>=6)
			sb.append("\n记录统计数据");
		else
			sb.append("\n不记录统计数据");
		if(!isFirstNight)
			sb.append("\n第一晚死亡不能发言");
		return sb.toString();
	}
	public static String getName(Class<? extends Villager> vcls) {
		for(Map.Entry<String,Class<? extends Villager>> me:caraMap.entrySet())
			if(me.getValue().equals(vcls))
				return me.getKey();
		return "错误角色";
	}
	public static List<Class<? extends Villager>> hunterRollRole(int cplayer){
		List<Class<? extends Villager>> roles=new ArrayList<>();
		int cwolf=(int) Math.ceil(cplayer*1/3D);
		int chunter=cplayer-cwolf;
		while(--cwolf>=0) {
			roles.add(Werewolf.class);
		}
		while(--chunter>=0) {
			roles.add(Hunter.class);
		}
		Collections.shuffle(roles);
		return roles;
	}
	public static List<Class<? extends Villager>> godFightRollRole(int cplayer){
		List<Class<? extends Villager>> roles=new ArrayList<>();
		roles.add(Witch.class);
		roles.add(Seer.class);
		roles.add(Idiot.class);
		roles.add(Defender.class);
		roles.add(WhiteWolf.class);
		roles.add(NightmareKnight.class);
		switch(cplayer) {
		case 6:break;
		case 7:roles.add(GraveKeeper.class);break;
		case 14:roles.add(Elder.class);
		case 13:roles.add(Tramp.class);
		case 12:roles.add(GraveKeeper.class);
		case 11:roles.add(Hunter.class);roles.add(DarkWolf.class);roles.add(Demon.class);roles.add(WolfKiller.class);roles.add(HiddenWolf.class);break;
		case 10:roles.add(Demon.class);
		case 9:roles.add(GraveKeeper.class);
		case 8:roles.add(Hunter.class);roles.add(DarkWolf.class);break;
		}
		Collections.shuffle(roles);
		return roles;
	}
	public static List<Class<? extends Villager>> fairRollRole(int cplayer){
		List<Class<? extends Villager>> rslt = null;
		double rsltpoint=100;
		for(int i=0;i<3;i++) {
			List<Class<? extends Villager>> cur=rollRole(cplayer);
			double curpoint=Math.abs(calculateRolePoint(cur)-0.3);
			if(curpoint<rsltpoint) {
				rslt=cur;
				rsltpoint=curpoint;
			}
		}
		return rslt;
	}
	public static double calculateRolePoint(List<Class<? extends Villager>> larr) {
		double rslt=0;
		for(Class<? extends Villager> cls:larr) {
			rslt+=rolePoint.get(cls);
		}
		return rslt;
	}
	public static List<Class<? extends Villager>> rollRole(int cplayer){
		List<Class<? extends Villager>> roles=new ArrayList<>();
		int godcount=(int) Math.ceil(cplayer/3.0);
		int wolfcount=(int)Math.ceil((cplayer-godcount)/2.0);
		int innocount=cplayer-godcount-wolfcount;
		if(innocount<wolfcount) {
			--innocount;
			roles.add(Elder.class);
		}
		List<Class<? extends Villager>> exwroles=new ArrayList<>();
		for(int i=0;i<wolfcount;i++)
			exwroles.add(Werewolf.class);
		if(cplayer>=8) {
			exwroles.add(Demon.class);
			exwroles.add(WhiteWolf.class);
			exwroles.add(DarkWolf.class);
			exwroles.add(HiddenWolf.class);
			exwroles.add(WolfBeauty.class);
			exwroles.add(Werewolf.class);
		}
		Collections.shuffle(exwroles);
		while(--wolfcount>=0)
			roles.add(exwroles.remove(0));
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
		exroles.add(Bear.class);
		exroles.add(Crow.class);
		exroles.add(Knight.class);
		exroles.add(GraveKeeper.class);
		exroles.add(WolfKiller.class);
		Collections.shuffle(exroles);
		while(--godcount>=0)
			roles.add(exroles.remove(0));
		Collections.shuffle(roles);
		return roles;
	}
	public String getWolfSentence() {
		if(canNoKill) {
			return "请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n投票之后“#要说的话”就会失效。\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n如果想空刀，请发送“放弃”";
		}
		return "请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n投票之后“#要说的话”就会失效。\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n如果想系统随机选择，请发送“放弃”";
	}
	public void executeNext() {
		if(next!=null)
			scheduler.execute(next);
	}

	//game control
	@Override
	protected void doFinalize() {
		vu.clear();
		for(Villager p:playerlist) {
			ListenerUtils.releaseListener(p.member.getId());
			GameUtils.RemoveMember(p.member.getId());
			
		}
		super.doFinalize();
		logger.sendLog(this.group);
	}
	@Override
	public void forceStop() {
		terminateWait(WaitReason.State);
		terminateWait(WaitReason.Vote);
		terminateWait(WaitReason.DieWord);
		terminateWait(WaitReason.Generic);
		StringBuilder mc=new StringBuilder("游戏已中断\n");
		mc.append("游戏身份：");
		for(Villager p:playerlist) {
			ListenerUtils.releaseListener(p.member.getId());
			GameUtils.RemoveMember(p.member.getId());
			mc.append("\n").append(p.getMemberString())
			.append("的身份为 ").append(p.getRole()).append(" ").append(DiedReason.getString(p.dr));
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
		mc.append("\n角色评分：").append(winrate);
		try {
			Thread.sleep(1000);//sbtx好像有频率限制，先等他个1秒再说
		} catch (InterruptedException e) {
		}
		this.sendPublicMessage(Utils.sendTextAsImage(mc.toString(),this.group));
		isEnded=true;
		super.forceStop();
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
		if(!GameUtils.tryAddMember(mem.getId())) {
			this.sendPublicMessage(new At(mem).plus("你已参加其他游戏！"));
			return true;
		}
		if(roles.size()>0) {
			try {
				synchronized(playerlist) {
					Villager cp;
					int min=playerlist.size();
					playerlist.add(cp=roles.remove(0).getConstructor(WerewolfGame.class,Member.class).newInstance(this,mem));
					
					cp.sendPrivate("已经报名");
					String nc=cp.member.getNameCard();
					if(nc.indexOf('|')!=-1) {
						nc=nc.split("\\|")[1];
					}
					if(min!=0) {
						cp.prev=playerlist.get(min-1);
						cp.prev.next=cp;
					}
					cp.member.setNameCard(min+"号 |"+nc);
					if(roles.size()==0) {
						cp.next=playerlist.get(0);
						cp.next.prev=cp;
						this.sendPublicMessage("狼人杀已满人，游戏即将开始。");
						scheduler.execute(()->gameStart());
					}
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
		Villager cp=playerlist.get(playerlist.size()-1);
		cp.next=playerlist.get(0);
		cp.next.prev=cp;
		scheduler.execute(()->gameStart());
	}
	//wait utils
	public void startWait(long millis,WaitReason lr) {
		wt[lr.getId()].startWait(millis);
	}
	public void skipWait(WaitReason lr) {
		wt[lr.getId()].stopWait();
	}
	public void terminateWait(WaitReason lr) {
		wt[lr.getId()].terminateWait();
	}
	public void endWait(WaitReason lr) throws InterruptedException{
		wt[lr.getId()].endWait();
	}
	//game logic
	void removeAllListeners() {
		for(Villager p:playerlist) {
			p.EndTurn();
			ListenerUtils.releaseListener(p.member.getId());
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
		logger.title("游戏开始");
		muteAll(true);
		this.sendPublicMessage(getGameRules());
		StringBuilder sb=new StringBuilder("玩家列表：\n");
		for(Villager p:playerlist) {
			p.onGameStart();
			sb.append(p.getMemberString());
			sb.append("\n");
		}
		this.sendPublicMessage(Utils.sendTextAsImage(sb.toString(),this.group));
		
		onDawn();
	}
	//混合循环
	public void nextOnDawn() {
		lastCursed=cursed;
		cursed=null;
		if(VictoryPending())return;
		onDawn();
	}
	public void onDawn() {
		day++;
		isDayTime=false;
		vu.clear();
		removeAllListeners();
		logger.logTurn(day,"狼人回合");
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
		vu.skipHalf=false;
		muteAll(true);
		for(Villager p:playerlist) {
			if(p.isDead)continue;
			p.onWolfTurn();
		}
		startWait(120000,WaitReason.Vote);
		removeAllListeners();
		List<Villager> il=vu.getForceMostVoted();
		if(il.size()>0) {
			logger.logSkill("狼人",il.get(0),"杀死");
			wolfKill(il.get(0));
		}else {
			if(canNoKill&&vu.finished()) {
				logger.logRaw("狼人空刀");
				scheduler.execute(()->afterWolf());
				return;
			}
			Villager rd;
			do {
				rd=playerlist.get((int)(Math.random()*playerlist.size()));
			}while(rd.isDead||rd.getFraction()==Fraction.Wolf);
			logger.logSkill("系统",rd,"随机杀死");
			wolfKill(rd);
		}
	}
	public void wolfKill(Villager p) {
		vu.clear();
		if(p instanceof Elder&&!((Elder) p).lifeUsed) {
			logger.logRaw("长老生命减少");
			((Elder) p).lifeUsed=true;
		}else if(p!=null)
			tokill.put(p,DiedReason.Wolf);
		afterWolf();
	}
	public void afterWolf() {
		logger.logTurn(day,"技能回合");
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
		logger.logTurn(day,"死亡技能回合");
		this.sendPublicMessage("有夜间技能的玩家请闭眼，有死亡技能的玩家请睁眼，你的技能状态是……");
		tokill.entrySet().removeIf(in->in.getKey().shouldSurvive(in.getValue()));
		for(Villager px:tokill.keySet()) {
			px.isDead=true;
		}
		for(Villager p2:playerlist) {
			if(p2 instanceof Bear&&p2.isDead)sendPublicMessage("昨晚熊没有咆哮。");
			if(p2.isDead)continue;
			p2.onTurn(4);
		}
		
		HashMap<Villager, DiedReason> tks=new HashMap<>(tokill);
		tokill.clear();
		Set<Entry<Villager, DiedReason>> rs=new HashSet<>(tks.entrySet());
		for(Entry<Villager, DiedReason> p:rs) {
			p.getKey().onDiePending(p.getValue());
		}
		startWait(30000,WaitReason.Generic);
		removeAllListeners();
		while(!tokill.isEmpty()) {
			rs=new HashSet<>(tokill.entrySet());
			tks.putAll(tokill);
			tokill.clear();
			boolean haswait=false;
			for(Entry<Villager, DiedReason> p:rs) {
				haswait|=p.getKey().onDiePending(p.getValue());
			}
			if(haswait)
			startWait(30000,WaitReason.Generic);
			removeAllListeners();
		}
		tokill.putAll(tks);
		scheduler.execute(()->onDayTime());
	}
	boolean isDayTime=false;
	public void skipDay() {
		terminateWait(WaitReason.State);
		terminateWait(WaitReason.Vote);
		terminateWait(WaitReason.DieWord);
		terminateWait(WaitReason.Generic);
		nextOnDawn();
	}
	public void preSkipDay() {
		terminateWait(WaitReason.State);
		terminateWait(WaitReason.Vote);
		terminateWait(WaitReason.DieWord);
		terminateWait(WaitReason.Generic);
	}
	public void onDayTime() {
		lastVoteOut=null;
		muteAll(false);
		logger.logTurn(day,"宣布死者");
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
				logger.logDeath(p.getKey(),p.getValue());
			}
		}else
			this.sendPublicMessage("昨夜无死者。");
		this.sendPublicMessage(Utils.sendTextAsImage(getAliveList(),this.group));
		tokill.clear();
		this.isFirstNight=false;
		for(Villager p:playerlist) {
			if(!p.isDead) {
				p.onTurnStart();
			}
		}
		isDayTime=true;
		logger.logTurn(day,"白天陈述");
		for(Villager p:playerlist) {
			if(!p.isDead) {
				p.onDayTime();
			}
		}
		vu.skipHalf=true;
		this.sendPublicMessage("你们有15秒思考时间，15秒后开始投票。");
		startWait(15000,WaitReason.Generic);
		muteAll(true);
		this.sendPublicMessage("请在两分钟内在私聊中完成投票！");
		logger.logTurn(day,"白天投票");
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
				logger.logTurn(day,"同票PK");
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
				logger.logDeath(pe.getKey(), pe.getValue());
			}
		}
		tokill.clear();
		canDayVote=false;
		nextOnDawn();
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
			else if(p instanceof WolfKiller)innos+=2;
		}
		boolean ends=false;
		String status=null;
		Fraction winfrac=null;
		if(innos==0&&wolfs>0) {
			status=("游戏结束！狼人获胜\n");
			ends=true;
			winfrac=Fraction.Wolf;
		}else if(wolfs==0&&innos>0) {
			status=("游戏结束！平民获胜\n");
			winfrac=Fraction.Innocent;
			ends=true;
		}else if(total==0){
			status=("游戏结束！同归于尽\n");
			ends=true;
		}else if((wolfs>=innos)) {
			status=("游戏结束！狼人获胜\n");
			winfrac=Fraction.Wolf;
			ends=true;
		}
		if(ends) {
			logger.title(status);
			GameData gd=null;
			if(doStat&&playerlist.size()>=6)
			gd=TableGames.db.getGame(getName());
			removeAllListeners();
			StringBuilder mc=new StringBuilder();
			mc.append(status);
			mc.append("游戏身份：");
			for(Villager p:playerlist) {
				mc.append("\n").append(p.getMemberString())
				.append("的身份为 ").append(p.getRole()).append(" ").append(DiedReason.getString(p.dr));
				String nc=p.member.getNameCard();
				if(nc.indexOf('|')!=-1) {
					nc=nc.split("\\|")[1];
				}
				p.member.setNameCard(nc);
				if(gd!=null) {
					WerewolfPlayerData wpd=gd.getPlayer(p.member.getId(),WerewolfPlayerData.class);
					wpd.log(p.getFraction(),winfrac,!p.isDead);
					gd.setPlayer(p.member.getId(),wpd);
				}
				try {
				if(p.isDead)p.member.unmute();
				}catch(Throwable t) {}
			}
			muteAll(false);
			mc.append("\n角色评分：").append(winrate);
			try {
				Thread.sleep(10000);//sbtx好像有频率限制，先等他个10秒再说
			} catch (InterruptedException e) {
			}
			this.sendPublicMessage(Utils.sendTextAsImage(mc.toString(),this.group));
			
			doFinalize();
			
		}
		isEnded=ends;
		return ends;
	}

}
