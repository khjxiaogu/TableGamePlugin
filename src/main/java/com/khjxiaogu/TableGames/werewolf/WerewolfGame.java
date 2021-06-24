package com.khjxiaogu.TableGames.werewolf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.TableGames;
import com.khjxiaogu.TableGames.data.PlayerDatabase.GameData;
import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.message.Message;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;

import com.khjxiaogu.TableGames.utils.ParamUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.VoteHelper;
import com.khjxiaogu.TableGames.utils.WaitThread;


public class WerewolfGame extends Game implements Serializable{

	private static final long serialVersionUID = 7731234732322205712L;

	public enum DiedReason {
		Vote("被驱逐", true, true, 0), Wolf("被杀死", true, false, 1), Poison("被毒死", false, false, 10),
		Hunter("被射死", false, false, 3), DarkWolf("被狼王杀死", true, false, 3), Knight("被单挑死", false, false, 3),
		Explode("自爆死", false, true, 3), Knight_s("以死谢罪", false, false, 3), Hunt("被猎杀", true, false, 2),
		Shoot("被箭射死", false, false, 2), Reflect("被反伤", false, false, 4), Love("殉情", false, false, 3),
		Shoot_s("射击失败", false, false, 3), Hunt_s("猎杀失败", false, false, 3), Burn("烧死", true, false, 1);

		String desc;
		final boolean canUseSkill;
		final boolean hasDiedWord;
		final int priority;

		private DiedReason(String desc, boolean canUseSkill, boolean hasDiedWord, int priority) {
			this.desc = desc;
			this.canUseSkill = canUseSkill;
			this.hasDiedWord = hasDiedWord;
			this.priority = priority;
		}

		@Override
		public String toString() {
			return desc;
		}

		public static String getString(DiedReason dr) {
			if (dr == null)
				return "存活";
			return dr.toString();
		}

		public boolean canBeReplaced(DiedReason dr) {
			return priority < dr.priority;
		}
	}

	public enum WaitReason {
		Generic(0), DieWord(1), State(2), Vote(3), Other(4);

		private final int id;

		private WaitReason(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	@FunctionalInterface
	public interface RoleRoller {
		List<Class<? extends Villager>> roll(int cplayer, double flag);
	}
	private static Map<Class<? extends Villager>, Double> rolePoint = new HashMap<>();
	private static Map<String, Class<? extends Villager>> caraMap = new HashMap<>();
	private static Map<String, RoleRoller> patterns = new HashMap<>();
	private static Map<Class<? extends Villager>, Class<? extends GenericBot>> botList = new HashMap<>();
	static {
		WerewolfGame.caraMap.put("乌鸦", Crow.class);
		WerewolfGame.caraMap.put("石像鬼", StatueDemon.class);
		WerewolfGame.caraMap.put("守墓人", GraveKeeper.class);
		WerewolfGame.caraMap.put("守卫", Defender.class);
		WerewolfGame.caraMap.put("猎人", Hunter.class);
		WerewolfGame.caraMap.put("白痴", Idiot.class);
		WerewolfGame.caraMap.put("平民", Villager.class);
		WerewolfGame.caraMap.put("骑士", Knight.class);
		WerewolfGame.caraMap.put("长老", Elder.class);
		WerewolfGame.caraMap.put("预言家", Seer.class);
		WerewolfGame.caraMap.put("老流氓", Tramp.class);
		WerewolfGame.caraMap.put("纵火者", Arsoner.class);
		WerewolfGame.caraMap.put("白狼王", WhiteWolf.class);
		WerewolfGame.caraMap.put("女巫", Witch.class);
		WerewolfGame.caraMap.put("狼人", Werewolf.class);
		WerewolfGame.caraMap.put("隐狼", HiddenWolf.class);
		WerewolfGame.caraMap.put("猎魔人", WolfKiller.class);
		WerewolfGame.caraMap.put("恶灵骑士", NightmareKnight.class);
		WerewolfGame.caraMap.put("狼王", DarkWolf.class);
		WerewolfGame.caraMap.put("狼美人", WolfBeauty.class);
		WerewolfGame.caraMap.put("熊", Bear.class);
		WerewolfGame.caraMap.put("验尸官", Coroner.class);
		WerewolfGame.caraMap.put("奇迹弓手", MiracleArcher.class);
		WerewolfGame.caraMap.put("恶魔",Demon.class);
		WerewolfGame.caraMap.put("巨狼",HardWolf.class);
		WerewolfGame.caraMap.put("狐狸",Fox.class);
		WerewolfGame.caraMap.put("禁言长老",Muter.class);
		WerewolfGame.rolePoint.put(Crow.class, 1D);
		WerewolfGame.rolePoint.put(StatueDemon.class, -0.5);
		WerewolfGame.rolePoint.put(GraveKeeper.class, 0.5);
		WerewolfGame.rolePoint.put(Defender.class, 1D);
		WerewolfGame.rolePoint.put(Hunter.class, 1D);
		WerewolfGame.rolePoint.put(Idiot.class, 0.5);
		WerewolfGame.rolePoint.put(Villager.class, 0D);
		WerewolfGame.rolePoint.put(Knight.class, 1.5);
		WerewolfGame.rolePoint.put(Elder.class, 0D);
		WerewolfGame.rolePoint.put(Seer.class, 1D);
		WerewolfGame.rolePoint.put(Tramp.class, 0D);
		WerewolfGame.rolePoint.put(Arsoner.class, 1.25);
		WerewolfGame.rolePoint.put(Coroner.class, 0.5);
		WerewolfGame.rolePoint.put(MiracleArcher.class, 1D);
		WerewolfGame.rolePoint.put(WhiteWolf.class, -1.5);
		WerewolfGame.rolePoint.put(Witch.class, 1D);
		WerewolfGame.rolePoint.put(Werewolf.class, -1D);
		WerewolfGame.rolePoint.put(HiddenWolf.class, -0.75D);
		WerewolfGame.rolePoint.put(WolfKiller.class, 1.5);
		WerewolfGame.rolePoint.put(NightmareKnight.class, -1.5);
		WerewolfGame.rolePoint.put(DarkWolf.class, -1.5);
		WerewolfGame.rolePoint.put(WolfBeauty.class, -1.5);
		WerewolfGame.rolePoint.put(Bear.class, 1D);
		WerewolfGame.rolePoint.put(Demon.class, -1.35);
		WerewolfGame.rolePoint.put(HardWolf.class, -1.25);
		WerewolfGame.rolePoint.put(Fox.class, 1.15D);
		WerewolfGame.rolePoint.put(Muter.class, 0.75D);
		WerewolfGame.botList.put(Crow.class, GenericBot.class);
		WerewolfGame.botList.put(StatueDemon.class, WereWolfBot.class);
		WerewolfGame.botList.put(GraveKeeper.class, GraveKeeperBot.class);
		WerewolfGame.botList.put(Defender.class, GenericBot.class);
		WerewolfGame.botList.put(Hunter.class, HunterBot.class);
		WerewolfGame.botList.put(Idiot.class, IdiotBot.class);
		WerewolfGame.botList.put(Villager.class, GenericBot.class);
		WerewolfGame.botList.put(Knight.class, GenericBot.class);
		WerewolfGame.botList.put(Elder.class, ElderBot.class);
		WerewolfGame.botList.put(Seer.class, SeerBot.class);
		WerewolfGame.botList.put(Tramp.class, GenericBot.class);
		WerewolfGame.botList.put(WhiteWolf.class, WereWolfBot.class);
		WerewolfGame.botList.put(Witch.class, WitchBot.class);
		WerewolfGame.botList.put(Arsoner.class, GenericBot.class);
		WerewolfGame.botList.put(Coroner.class, GenericBot.class);
		WerewolfGame.botList.put(MiracleArcher.class, GenericBot.class);
		WerewolfGame.botList.put(Werewolf.class, WereWolfBot.class);
		WerewolfGame.botList.put(HiddenWolf.class, WereWolfBot.class);
		WerewolfGame.botList.put(WolfKiller.class, WolfKillerBot.class);
		WerewolfGame.botList.put(NightmareKnight.class, WereWolfBot.class);
		WerewolfGame.botList.put(DarkWolf.class, DarkWolfBot.class);
		WerewolfGame.botList.put(WolfBeauty.class, WereWolfBot.class);
		WerewolfGame.botList.put(Bear.class, BearBot.class);
		WerewolfGame.botList.put(Demon.class, WereWolfBot.class);
		WerewolfGame.botList.put(HardWolf.class, WereWolfBot.class);
		WerewolfGame.botList.put(Fox.class, GenericBot.class);
		WerewolfGame.botList.put(Muter.class, GenericBot.class);
		WerewolfGame.patterns.put("默认", (cp, obj) -> WerewolfGame.fairRollRole(cp));
		WerewolfGame.patterns.put("随机", (cp, obj) -> WerewolfGame.rollRole(cp));
		WerewolfGame.patterns.put("诸神", (cn, obj) -> WerewolfGame.godFightRollRole(cn));
		WerewolfGame.patterns.put("猎人", (cp, obj) -> WerewolfGame.hunterRollRole(cp));
	}
	transient Set<Villager> tokill = Collections.newSetFromMap(new ConcurrentHashMap<>());
	int[] tokillIds;
	transient List<Villager> sherifflist = Collections.synchronizedList(new ArrayList<>());
	List<Villager> playerlist = Collections.synchronizedList(new ArrayList<>());
	transient List<Villager> canVote = Collections.synchronizedList(new ArrayList<>());
	transient List<Villager> canTalk=Collections.synchronizedList(new ArrayList<>());
	double winrate = 0;
	WerewolfGameLogger logger = new WerewolfGameLogger();
	boolean isDayTime = false;
	boolean isFirstNight = true;
	boolean canDayVote = false;
	boolean isEnded = false;
	boolean sameTurn = false;
	boolean canNoKill = false;
	boolean hunterMustShoot = false;
	boolean doStat = true;
	boolean hasTramp = false;
	boolean hasElder = false;
	boolean hasSheriff=false;
	boolean isSheriffSelection=false;
	boolean isSkippedDay=false;
	int day = 0;
	int lastDeathCount = 0;

	transient Villager lastVoteOut;
	int lastVoteOutId;

	transient Villager cursed = null;
	int cursedId;

	transient Villager lastCursed = null;
	int lastCursedId;

	transient Villager lastwolfkill = null;
	int lastwolfkillId;

	transient Object waitLock = new Object();
	transient VoteHelper<Villager> vu = new VoteHelper<>();
	int num = 0;
	int pointpool = -1;
	transient WaitThread[] wt = new WaitThread[5];
	transient public List<Class<? extends Villager>> roles;
	@Override
	public boolean specialCommand(AbstractPlayer m,String[] cmds) {
		if(cmds.length==1) {
			if(cmds[0].equals("game")) {
				m.sendPrivate(String.join(",",ParamUtils.loadParams(this)));
				return true;
			}
		}
		if(cmds.length==2) {
			if(cmds[0].equals("game")) {
				m.sendPrivate(ParamUtils.getValue(this,cmds[1]));
				return true;
			}else if(cmds[0].equals("role")) {
				m.sendPrivate(String.join(",",ParamUtils.loadParams(getPlayerById(Long.parseLong(cmds[1])))));
				return true;
			}
		}
		if(cmds.length==3) {
			if(cmds[0].equals("game")) {
				ParamUtils.setValue(this,cmds[1],cmds[2]);
				m.sendPrivate(ParamUtils.getValue(this,cmds[1]));
				return true;
			}else if(cmds[0].equals("role")) {
				m.sendPrivate(ParamUtils.getValue(getPlayerById(Long.parseLong(cmds[1])),cmds[2]));
				return true;
			}
		}
		if(cmds.length==4) {
			if(cmds[0].equals("role")) {
				ParamUtils.setValue(getPlayerById(Long.parseLong(cmds[1])),cmds[2],cmds[3]);
				m.sendPrivate(ParamUtils.getValue(getPlayerById(Long.parseLong(cmds[1])),cmds[2]));
				return true;
			}
		}
		return false;
	}
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		// perform the default de-serialization first
		aInputStream.defaultReadObject();
		wt = new WaitThread[5];
		vu = new VoteHelper<>();
		waitLock = new Object();
		tokill = Collections.newSetFromMap(new ConcurrentHashMap<>());
		for (int i = 0; i < wt.length; i++) {
			wt[i] = new WaitThread();
		}
		Villager prev=playerlist.get(playerlist.size()-1);
		int min=0;
		for(Villager v:playerlist) {
			prev.next=v;
			v.prev=prev;
			prev=v;
			v.game=this;
			v.retake();
			String nc = v.getNameCard();
			if (nc.indexOf('|') != -1) {
				nc = nc.split("\\|")[1];
			}
			v.setNameCard(min++ + "号 |" + nc);
		}
		lastVoteOut=getPlayerById(lastVoteOutId);
		cursed=getPlayerById(cursedId);
		lastCursed=getPlayerById(lastCursedId);
		lastwolfkill=getPlayerById(lastwolfkillId);
		tokillIds=new int[tokill.size()];
		canTalk=Collections.synchronizedList(new ArrayList<>());
		sherifflist = Collections.synchronizedList(new ArrayList<>());
		canVote = Collections.synchronizedList(new ArrayList<>());
		for(int tok:tokillIds) {
			tokill.add(getPlayerById(tok));
		}
		this.sendPublicMessage("狼人杀游戏将在20秒后继续，请各位做好准备");
		getScheduler().execute(()->{
			try {
				Thread.sleep(20000);
			}catch(InterruptedException ie){
			}
			onDawnNoSe();
		});
		// ensure that object state has not been corrupted or tampered with malicious code
		//validateUserInfo();
	}

	/**
	 * This is the default implementation of writeObject. Customize as necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

		//ensure that object is in desired state. Possibly run any business rules if applicable.
		//checkUserInfo();

		// perform the default serialization for all non-transient, non-static fields
		lastVoteOutId=getIdByPlayer(lastVoteOut);
		cursedId=getIdByPlayer(cursed);
		lastCursedId=getIdByPlayer(lastCursed);
		lastwolfkillId=getIdByPlayer(lastwolfkill);
		tokillIds=new int[tokill.size()];
		int i=0;
		for(Villager tok:tokill) {
			tokillIds[i++]=getIdByPlayer(tok);
		}
		aOutputStream.defaultWriteObject();
	}
	public WerewolfGame(AbstractRoom g, int cplayer) {
		super(g, cplayer, cplayer * 2);
		for (int i = 0; i < wt.length; i++) {
			wt[i] = new WaitThread();
		}
		roles = Collections.synchronizedList(WerewolfGame.fairRollRole(cplayer));
		winrate = WerewolfGame.calculateRolePoint(roles);
		if(cplayer>=9){
			hasSheriff=true;
		}
		for (Class<? extends Villager> role : roles) {
			if (Tramp.class.isAssignableFrom(role)) {
				hasTramp = true;
				continue;
			}
			if (Elder.class.isAssignableFrom(role)) {
				hasElder = true;
				continue;
			}
		}
	}

	public WerewolfGame(AbstractRoom g, String... args) {
		super(g, args.length, args.length * 2);
		for (int i = 0; i < wt.length; i++) {
			wt[i] = new WaitThread();
		}
		roles = Collections.synchronizedList(new ArrayList<>());
		for (String s : args) {
			roles.add(WerewolfGame.caraMap.getOrDefault(s, Villager.class));
		}
		Collections.shuffle(roles);
		if(roles.size()>=9){
			hasSheriff=true;
		}
		winrate = WerewolfGame.calculateRolePoint(roles);
		for (Class<? extends Villager> role : roles) {
			if (Tramp.class.isAssignableFrom(role)) {
				hasTramp = true;
				continue;
			}
			if (Elder.class.isAssignableFrom(role)) {
				hasElder = true;
				continue;
			}
		}
	}

	public WerewolfGame(AbstractRoom g, int cplayer, Map<String, String> sets) {
		super(g, cplayer, cplayer * 2);
		int botnm = 0;
		for (int i = 0; i < wt.length; i++) {
			wt[i] = new WaitThread();
		}
		if (sets.containsKey("机器人")) {
			cplayer += botnm = Integer.parseInt(sets.get("机器人"));
		} else if (sets.containsKey("人数")) {
			int tplayer = Integer.parseInt(sets.get("人数"));
			botnm = tplayer - cplayer;
			cplayer = tplayer;
		}
		double cpoint = Double.parseDouble(sets.getOrDefault("评分", "0.3"));
		roles = Collections.synchronizedList(WerewolfGame.patterns
				.getOrDefault(sets.getOrDefault("板", "默认"), (cp, cps) -> WerewolfGame.fairRollRole(cp, cps))
				.roll(cplayer, cpoint));
		canNoKill = sets.getOrDefault("空刀", "false").equals("true");
		hunterMustShoot = sets.getOrDefault("压枪", "false").equals("true");
		doStat = sets.getOrDefault("统计", "true").equals("true");
		isFirstNight = sets.getOrDefault("首夜发言", "true").equals("true");
		pointpool = Integer.parseInt(sets.getOrDefault("积分奖池", "-1"));
		boolean isDeadBot=sets.getOrDefault("不接管", "false").equals("true");
		Collections.shuffle(roles);
		winrate = WerewolfGame.calculateRolePoint(roles);
		hasSheriff= sets.getOrDefault("警长",String.valueOf(cplayer>=9)).equals("true");
		for (Class<? extends Villager> role : roles) {
			if (Tramp.class.isAssignableFrom(role)) {
				hasTramp = true;
				continue;
			}
			if (Elder.class.isAssignableFrom(role)) {
				hasElder = true;
				continue;
			}
		}
		if (botnm > 0) {
			while (botnm-- > 0) {
				synchronized (playerlist) {
					Villager cp = null;
					int min = playerlist.size();
					Class<? extends Villager> role = roles.remove(0);
					Class<? extends GenericBot> bot = WerewolfGame.botList.get(role);
					try {
						if(!isDeadBot) {
							playerlist.add(cp = role.getConstructor(WerewolfGame.class, AbstractPlayer.class).newInstance(
									this, bot.getConstructor(int.class, WerewolfGame.class).newInstance(min, this)));
						} else {
							playerlist.add(cp = role.getConstructor(WerewolfGame.class, AbstractPlayer.class).newInstance(
									this,new DeadBot(min,this)));
						}
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | NoSuchMethodException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

					cp.sendPrivate("已经报名");
					String nc = cp.getNameCard();
					if (nc.indexOf('|') != -1) {
						nc = nc.split("\\|")[1];
					}
					if (min != 0) {
						cp.prev = playerlist.get(min - 1);
						cp.prev.next = cp;
					}
					cp.setNameCard(min + "号 |" + nc);
					if (roles.size() == 0) {
						cp.next = playerlist.get(0);
						cp.next.prev = cp;
						this.sendPublicMessage("狼人杀已满人，游戏即将开始。");
						getScheduler().execute(() -> gameStart());
					}
				}
			}
		}
	}

	public String getGameRules() {
		StringBuilder sb = new StringBuilder("游戏规则设定：");
		if (canNoKill) {
			sb.append("\n允许狼人空刀");
		} else {
			sb.append("\n狼人必须杀人");
		}
		if (hasTramp) {
			sb.append("\n有老流氓");
		}
		if (hasElder) {
			sb.append("\n有长老");
		}
		if (roles.size() + playerlist.size() >= 8) {
			sb.append("\n允许狼神");
		}
		if (hunterMustShoot) {
			sb.append("\n猎人不能压枪");
		}
		if(hasSheriff) {
			sb.append("\n首日产生警长");
		}
		sb.append("\n人数：").append(playerlist.size());
		int inno=0,wolf=0,god=0;
		for(Villager v:playerlist) {
			if(v.getFraction()==Fraction.Wolf) {
				wolf++;
			} else if(v.getFraction()==Fraction.God) {
				god++;
			} else {
				inno++;
			}
		}
		sb.append("\n神/狼/民：").append(god).append("/").append(wolf).append("/").append(inno);
		if (doStat && roles.size() + playerlist.size() >= 6) {
			sb.append("\n记录统计数据");
		} else {
			sb.append("\n不记录统计数据");
		}
		if (!isFirstNight) {
			sb.append("\n第一晚死亡不能发言");
		}
		if (doStat && pointpool == -1) {
			int cplayer = playerlist.size();
			if (cplayer >= 6) {
				pointpool = cplayer - (int) Math.ceil((cplayer - (int) Math.ceil(cplayer / 3.0)) / 2.0)
						+ playerlist.size() - 6;
			} else {
				pointpool = 0;
			}
		}
		if (pointpool > 0) {
			sb.append("\n获胜奖池：").append(pointpool);
		}

		return sb.toString();
	}

	public static String getName(Class<? extends Villager> vcls) {
		for (Map.Entry<String, Class<? extends Villager>> me : WerewolfGame.caraMap.entrySet())
			if (me.getValue().equals(vcls))
				return me.getKey();
		return "错误角色";
	}

	public static List<Class<? extends Villager>> hunterRollRole(int cplayer) {
		List<Class<? extends Villager>> roles = new ArrayList<>();
		int cwolf = (int) Math.ceil(cplayer * 1 / 3D);
		int chunter = cplayer - cwolf;
		while (--cwolf >= 0) {
			roles.add(Werewolf.class);
		}
		while (--chunter >= 0) {
			roles.add(Hunter.class);
		}
		Collections.shuffle(roles);
		return roles;
	}

	public static List<Class<? extends Villager>> godFightRollRole(int cplayer) {
		List<Class<? extends Villager>> roles = new ArrayList<>();
		roles.add(Witch.class);
		roles.add(Seer.class);
		roles.add(Idiot.class);
		roles.add(Defender.class);
		roles.add(WhiteWolf.class);
		roles.add(NightmareKnight.class);
		switch (cplayer) {
		case 6:
			break;
		case 7:
			roles.add(GraveKeeper.class);
			break;
		case 14:
			roles.add(Elder.class);
		case 13:
			roles.add(Tramp.class);
		case 12:
			roles.add(GraveKeeper.class);
		case 11:
			roles.add(Hunter.class);
			roles.add(DarkWolf.class);
			roles.add(StatueDemon.class);
			roles.add(WolfKiller.class);
			roles.add(HiddenWolf.class);
			break;
		case 10:
			roles.add(StatueDemon.class);
		case 9:
			roles.add(GraveKeeper.class);
		case 8:
			roles.add(Hunter.class);
			roles.add(DarkWolf.class);
			break;
		}
		Collections.shuffle(roles);
		return roles;
	}

	public static List<Class<? extends Villager>> fairRollRole(int cplayer) {
		return WerewolfGame.fairRollRole(cplayer, 0.3);
	}

	public static List<Class<? extends Villager>> fairRollRole(int cplayer, double cps) {
		List<Class<? extends Villager>> rslt = null;
		double rsltpoint = 100;
		for (int i = 0; i < 3; i++) {
			List<Class<? extends Villager>> cur = WerewolfGame.rollRole(cplayer);
			double curpoint = Math.abs(WerewolfGame.calculateRolePoint(cur) - cps);
			if (curpoint < rsltpoint) {
				rslt = cur;
				rsltpoint = curpoint;
			}
		}
		return rslt;
	}

	public static double calculateRolePoint(List<Class<? extends Villager>> larr) {
		double rslt = 0;
		for (Class<? extends Villager> cls : larr) {
			rslt += WerewolfGame.rolePoint.get(cls);
		}
		return rslt;
	}

	public static List<Class<? extends Villager>> rollRole(int cplayer) {
		List<Class<? extends Villager>> roles = new ArrayList<>();
		int godcount = (int) Math.ceil(cplayer / 3.0);
		int wolfcount = (int) Math.ceil((cplayer - godcount) / 2.0);
		int innocount = cplayer - godcount - wolfcount;
		if (innocount < wolfcount) {
			--innocount;
			roles.add(Elder.class);
		}
		List<Class<? extends Villager>> exwroles = new ArrayList<>();
		for (int i = 0; i < wolfcount; i++) {
			exwroles.add(Werewolf.class);
		}
		if (cplayer >= 8) {
			exwroles.add(StatueDemon.class);
			exwroles.add(WhiteWolf.class);
			exwroles.add(DarkWolf.class);
			exwroles.add(HiddenWolf.class);
			exwroles.add(WolfBeauty.class);
			exwroles.add(Werewolf.class);
			exwroles.add(Demon.class);
			exwroles.add(HardWolf.class);
		}
		Collections.shuffle(exwroles);
		while (--wolfcount >= 0) {
			roles.add(exwroles.remove(0));
		}
		if (innocount >= 3) {
			roles.add(Tramp.class);
			--innocount;
		}
		while (--innocount >= 0) {
			roles.add(Villager.class);
		}
		Collections.shuffle(roles);
		List<Class<? extends Villager>> exroles = new ArrayList<>();
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
		exroles.add(Arsoner.class);
		exroles.add(Coroner.class);
		exroles.add(MiracleArcher.class);
		exroles.add(Fox.class);
		exroles.add(Muter.class);
		Collections.shuffle(exroles);
		while (--godcount >= 0) {
			roles.add(exroles.remove(0));
		}
		Collections.shuffle(roles);
		return roles;
	}

	public String getWolfSentence() {
		if (canNoKill)
			return "请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n如果想空刀，请发送“放弃”";
		return "请私聊选择要杀的人，你有2分钟的考虑时间\n也可以通过“#要说的话”来给所有在场狼人发送信息\n格式：“投票 qq号或者游戏号码”\n如：“投票 1”\n如果想系统随机选择，请发送“放弃”";
	}



	// game control
	@Override
	protected void doFinalize() {
		vu.clear();
		for (Villager p : playerlist) {
			p.releaseListener();
			GameUtils.RemoveMember(p.getId());

		}
		super.doFinalize();

	}

	@Override
	public void forceStop() {
		terminateWait(WaitReason.State);
		terminateWait(WaitReason.Vote);
		terminateWait(WaitReason.DieWord);
		terminateWait(WaitReason.Generic);
		StringBuilder mc = new StringBuilder("游戏已中断\n");
		mc.append("游戏身份：");
		for (Villager p : playerlist) {
			p.releaseListener();
			GameUtils.RemoveMember(p.getId());
			mc.append("\n").append(p.getMemberString()).append("的身份为 ").append(p.getRole()).append(" ")
			.append(DiedReason.getString(p.getEffectiveDiedReason()));
			String nc = p.getNameCard();
			if (nc.indexOf('|') != -1) {
				nc = nc.split("\\|")[1];
			}
			p.setNameCard(nc);
			try {
				p.tryUnmute();
			} catch (Throwable t) {
			}

		}
		// muteAll(false);
		mc.append("\n角色评分：").append(winrate);
		try {
			Thread.sleep(1000);// sbtx好像有频率限制，先等他个1秒再说
		} catch (InterruptedException e) {
		}
		this.sendPublicMessage(Utils.sendTextAsImage(mc.toString(), getGroup()));
		isEnded = true;
		logger.sendLog(getGroup());
		super.forceStop();
	}
	@Override
	public void forceInterrupt() {
		terminateWait(WaitReason.State);
		terminateWait(WaitReason.Vote);
		terminateWait(WaitReason.DieWord);
		terminateWait(WaitReason.Generic);
		for (Villager p : playerlist) {
			p.releaseListener();
			GameUtils.RemoveMember(p.getId());
			String nc = p.getNameCard();
			if (nc.indexOf('|') != -1) {
				nc = nc.split("\\|")[1];
			}
			p.setNameCard(nc);
			try {
				p.tryUnmute();
			} catch (Throwable t) {
			}

		}
		try {
			Thread.sleep(1000);// sbtx好像有频率限制，先等他个1秒再说
		} catch (InterruptedException e) {
		}
		this.sendPublicMessage("游戏已暂停，请等待恢复");
		isEnded = true;
		super.forceStop();
	}
	@Override
	public void forceSkip() {
		skipWait(WaitReason.State);
		skipWait(WaitReason.Vote);
		skipWait(WaitReason.DieWord);
		skipWait(WaitReason.Generic);
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
		for (Villager in : playerlist) {
			if (in.onReattach(c))
				return true;
		}
		return false;
	}

	public String getAliveList() {
		StringBuilder sb = new StringBuilder("存活：\n");
		for (Villager p : playerlist) {
			if (!p.isDead) {
				sb.append(p.getMemberString());
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public int getAliveCount() {
		int cnt = 0;
		for (Villager p : playerlist) {
			if (!p.isDead) {
				cnt++;
			}
		}
		return cnt;
	}

	@Override
	public boolean addMember(AbstractPlayer mem) {
		if (getPlayerById(mem.getId()) != null) {
			mem.sendPublic("你已经报名了！");
			return false;
		}
		if (!GameUtils.tryAddMember(mem.getId())) {
			mem.sendPublic("你已参加其他游戏！");
			return true;
		}
		if (roles.size() > 0) {
			try {
				synchronized (playerlist) {
					Villager cp;
					int min = playerlist.size();
					playerlist.add(cp = roles.remove(0).getConstructor(WerewolfGame.class, AbstractPlayer.class)
							.newInstance(this, mem));

					cp.sendPrivate("已经报名");
					String nc = cp.getNameCard();
					if (nc.indexOf('|') != -1) {
						nc = nc.split("\\|")[1];
					}
					if (min != 0) {
						cp.prev = playerlist.get(min - 1);
						cp.prev.next = cp;
					}
					cp.setNameCard(min + "号 |" + nc);
					if (roles.size() == 0) {
						cp.next = playerlist.get(0);
						cp.next.prev = cp;
						this.sendPublicMessage("狼人杀已满人，游戏即将开始。");
						getScheduler().execute(() -> gameStart());
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
		Villager cp = playerlist.get(playerlist.size() - 1);
		cp.next = playerlist.get(0);
		cp.next.prev = cp;
		getScheduler().execute(() -> gameStart());
	}

	@Override
	public void forceShow(AbstractPlayer ct) {
		StringBuilder mc = new StringBuilder("游戏身份：");
		for (Villager p : playerlist) {
			mc.append("\n").append(p.getMemberString()).append("的身份为 ").append(p.getRole()).append(" ")
			.append(DiedReason.getString(p.getEffectiveDiedReason()));
		}
		mc.append("\n角色评分：").append(winrate);
		try {
			Thread.sleep(1000);// sbtx好像有频率限制，先等他个1秒再说
		} catch (InterruptedException e) {
		}
		ct.sendPrivate(mc.toString());
	}

	@Override
	public boolean takeOverMember(long id, AbstractPlayer o) {
		Villager p = getPlayerById(id);
		try {
			int n = playerlist.indexOf(p);
			String nc = p.getNameCard();
			if (nc.indexOf('|') != -1) {
				nc = nc.split("\\|")[1];
			}
			p.setNameCard(nc);
			if (o == null) {
				p.doTakeOver(WerewolfGame.botList.get(p.getClass()).getConstructor(int.class, WerewolfGame.class)
						.newInstance(n, this));
			} else {
				p.doTakeOver(o);
			}
			p.setNameCard(n + "号 |" + p.getNameCard());
			return true;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	// wait utils
	public void startWait(long millis, WaitReason lr) {
		wt[lr.getId()].startWait(millis);
	}

	public void skipWait(WaitReason lr) {
		wt[lr.getId()].stopWait();
	}

	public void terminateWait(WaitReason lr) {
		wt[lr.getId()].terminateWait();
	}

	public void endWait(WaitReason lr) throws InterruptedException {
		wt[lr.getId()].endWait();
	}

	// game logic
	void removeAllListeners() {
		for (Villager p : playerlist) {
			p.EndTurn();
			p.releaseListener();
		}
	}
	public int getIdByPlayer(Villager v) {
		int i=0;
		for (Villager p : playerlist) {
			if (p==v)
				return i;
			i++;
		}
		return -1;
	}
	public Villager getPlayerById(long id) {
		int i = 0;
		for (Villager p : playerlist) {
			if (p.getId() == id || i == id)
				return p;
			i++;
		}
		return null;
	}

	public void WolfVote(Villager src, Villager id) {
		if (vu.vote(src, id)) {
			skipWait(WaitReason.Vote);
		}
	}

	public void DayVote(Villager src, Villager id) {
		if (canDayVote)
			if (vu.vote(src, id,src.getTicketCount())) {
				skipWait(WaitReason.Vote);
			}
	}

	public boolean checkCanVote(Villager id) {
		if (canVote == null)
			return true;
		return canVote.contains(id);
	}

	public void NoVote(Villager src) {
		vu.giveUp(src);
		if (vu.finished()) {
			skipWait(WaitReason.Vote);
		}
	}

	void kill(Villager p, DiedReason r) {
		tokill.add(p);
		p.populateDiedReason(r);
	}

	/**
	 * @param isMute
	 */
	@SuppressWarnings("unused")
	private void muteAll(boolean isMute) {
		getGroup().setMuteAll(isMute);
	}

	// 开始游戏流程
	public void gameStart() {
		logger.title("游戏开始");
		// muteAll(true);
		this.sendPublicMessage(getGameRules());
		StringBuilder sb = new StringBuilder("玩家列表：\n");
		for (Villager p : playerlist) {
			p.onGameStart();
			sb.append(p.getMemberString());
			sb.append("\n");
			p.onFinishTalk();
		}
		this.sendPublicMessage(Utils.sendTextAsImage(sb.toString(), getGroup()));

		onDawn();
	}

	// 混合循环
	public void nextOnDawn() {
		isFirstNight=false;
		lastCursed = cursed;
		cursed = null;
		if (VictoryPending())
			return;
		onDawn();
	}
	public void onDawn() {
		try {
			FileOutputStream fileOut = new FileOutputStream(new File(TableGames.plugin.getDataFolder(),""+getGroup()+".game"));
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onDawnNoSe();
	}
	public void onDawnNoSe() {
		day++;
		isDayTime = false;
		vu.clear();
		removeAllListeners();
		logger.logTurn(day, "狼人回合");
		onWolfTurn();
	}

	/*
	 * public void onUpperNightTurn() {
	 * this.sendPublicMessage("天黑了，所有人闭眼，有上半夜技能的玩家请睁眼，请私聊决定技能……");
	 * for(Innocent p2:playerlist) {
	 * if(p2.isDead)continue;
	 * p2.onTurn(4);
	 * }
	 * startWait(30000);
	 * removeAllListeners();
	 * scheduler.execute(()->onWolfTurn());
	 * }
	 */
	public void onWolfTurn() {
		sendPublicMessage("天黑了，所有人闭眼，狼人请睁眼，请私聊投票选择你们要杀的人。");
		vu.skipHalf = false;
		// muteAll(true);
		for (Villager p : playerlist) {
			if (p.isDead) {
				continue;
			}
			p.lastIsMuted=false;
			if(p.isMuted) {
				p.isMuted=false;
				p.lastIsMuted=true;
			}
			p.onWolfTurn();
		}
		startWait(120000, WaitReason.Vote);
		removeAllListeners();
		List<Villager> il = vu.getForceMostVoted();
		if (il.size() > 0) {

			wolfKill(il.get(0));
		} else {
			if (canNoKill && vu.finished()) {
				logger.logRaw("狼人空刀");
				lastwolfkill = null;
				getScheduler().execute(() -> afterWolf());
				return;
			}
			Villager rd;
			do {
				rd = playerlist.get((int) (Math.random() * playerlist.size()));
			} while (rd.isDead || rd.getFraction() == Fraction.Wolf);
			logger.logSkill("系统", rd, "随机杀死");
			wolfKill(rd);
		}
	}

	public void wolfKill(Villager p) {
		logger.logSkill("狼人",p, "杀死");
		vu.clear();
		lastwolfkill = p;
		for(Villager px:playerlist) {
			if(!px.isDead&&px.canWolfTurn()) {
				px.sendPrivate("昨晚最终决定杀死"+p.getMemberString());
			}
		}
		if (p instanceof Elder && !((Elder) p).lifeUsed) {
			logger.logRaw("长老生命减少");
			((Elder) p).lifeUsed = true;
		} else if (p != null) {
			kill(p, DiedReason.Wolf);
		}
		afterWolf();
	}

	public void afterWolf() {
		logger.logTurn(day, "技能回合");
		this.sendPublicMessage("狼人请闭眼，有夜间技能的玩家请睁眼，请私聊选择你们的技能。");
		for (Villager p2 : playerlist) {
			if (p2.isDead) {
				continue;
			}
			p2.onTurn(2);
		}
		startWait(60000, WaitReason.Generic);
		removeAllListeners();
		if(isFirstNight&&hasSheriff) {
			getScheduler().execute(() -> onSheriffSelect());
			return;
		}
		getScheduler().execute(() -> onDiePending());
	}
	public void onSheriffSelect() {
		logger.logTurn(day, "警长竞选");
		isSheriffSelection=true;
		this.sendPublicMessage("所有人睁眼，警长竞选回合开始。");
		for(Villager v:playerlist) {
			v.onSelectSheriff();
		}
		startWait(60000,WaitReason.Generic);
		if(sherifflist.size()==0) {
			this.sendPublicMessage("无人竞选，跳过环节。");
			getScheduler().execute(() -> onDiePending());
			return;
		}
		List<Villager> restToVote=new ArrayList<>(playerlist);
		Collections.shuffle(sherifflist);
		StringBuilder sb = new StringBuilder("警长竞选列表：\n");
		for(Villager p:playerlist) {
			if(p instanceof Werewolf) {
				p.sendPrivate(p.getRole()+"，你可以在投票前随时翻牌自爆并且立即进入黑夜，格式：“自爆”");
				p.addDaySkillListener();
			}
		}
		if(restToVote.isEmpty()) {
			this.sendPublicMessage("无人可以投票，若投票前有多于一人竞选，则无人可以得到警徽。");
		}
		for (Villager p : sherifflist) {
			restToVote.remove(p);
			p.onBeforeSheriffState();
			sb.append(p.getMemberString());
			sb.append("\n");
		}
		this.sendPublicMessage(Utils.sendTextAsImage(sb.toString(), getGroup()));
		for(Villager v:new ArrayList<>(sherifflist)) {
			if(!sherifflist.contains(v)) {
				continue;
			}
			v.onSheriffState();
		}
		this.sendPublicMessage("你们有15秒思考时间，15秒后开始投票。");
		if(sherifflist.size()>1) {
			if(restToVote.isEmpty()) {
				this.sendPublicMessage("无人可以投票，跳过环节。");
				getScheduler().execute(() -> onDiePending());
				return;
			}

			startWait(15000,WaitReason.Generic);
			vu.clear();
			restToVote.forEach(v->v.onSheriffVote());
			this.sendPublicMessage("请在两分钟内在私聊中完成投票！");
			vu.hintVote(getScheduler());
			startWait(120000,WaitReason.Vote);
			voteSheriff(vu.getForceMostVoted(),restToVote);
			return;
		}else if(sherifflist.size()==1){
			Villager slt=sherifflist.get(0);
			this.sendPublicMessage("仅一人竞选，"+slt.getMemberString()+"直接当选！");
			slt.isSheriff=true;

		}else if(sherifflist.size()==0) {
			this.sendPublicMessage("无人竞选，跳过环节。");
		}
		for(Villager p:playerlist) {
			p.releaseListener();
		}

		getScheduler().execute(() -> onDiePending());
	}
	public void voteSheriff(List<Villager> ps,List<Villager> vtb) {
		vu.clear();
		if (ps.size() > 1) {
			if (!sameTurn) {
				sherifflist.clear();
				sherifflist.addAll(ps);
				logger.logTurn(day, "警长同票PK");
				sameTurn = true;
				this.sendPublicMessage("同票，请做最终陈述。");
				Message mcb = new Message();
				mcb.append("开始投票，请在两分钟内投给以下人物其中之一：\n");
				canVote.clear();
				canVote.addAll(ps);
				for (Villager p : ps) {
					mcb.append(p.getAt());
					mcb.append("\n");
					p.onSheriffState();
				}
				if(sherifflist.size()==1){
					Villager slt=sherifflist.get(0);
					this.sendPublicMessage("仅一人竞选，"+slt.getMemberString()+"直接当选！");
					slt.isSheriff=true;
					getScheduler().execute(() -> onDiePending());
					return;
				}else if(sherifflist.size()==0) {
					this.sendPublicMessage("无人竞选，跳过环节。");
					getScheduler().execute(() -> onDiePending());
					return;
				}
				mcb.append("请在两分钟内在私聊中完成投票！");
				this.sendPublicMessage(mcb);
				// muteAll(true);
				vtb.forEach(v->v.onSheriffVote());
				vu.hintVote(getScheduler());
				getScheduler().execute(() -> {
					startWait(120000, WaitReason.Vote);
					removeAllListeners();
					voteSheriff(vu.getForceMostVoted(),vtb);
				});
				return;
			}
			this.sendPublicMessage("再次同票，警徽流失。");
			ps.clear();
		}
		canVote.clear();
		sameTurn = false;
		if (ps.size() == 0) {
			this.sendPublicMessage("无人当选");
		} else {
			Villager p = ps.get(0);
			this.sendPublicMessage(p.getMemberString()+"当选！");
			p.isSheriff=true;
		}
		getScheduler().execute(() -> onDiePending());
	}
	public void SheriffVote(Villager src, Villager id) {
		if (vu.vote(src, id)) {
			skipWait(WaitReason.Vote);
		}
	}
	public void onDiePending() {
		isSheriffSelection=false;
		logger.logTurn(day, "死亡技能回合");
		this.sendPublicMessage("有夜间技能的玩家请闭眼，有死亡技能的玩家请睁眼，你的技能状态是……");
		if (lastwolfkill.isBurned) {
			lastwolfkill.isBurned = false;
			Villager firstWolf = lastwolfkill.prev;
			while (firstWolf != lastwolfkill) {
				if (firstWolf.isDead&&firstWolf instanceof Werewolf) {
					break;
				}
			}
			if (firstWolf == lastwolfkill) {
				firstWolf = lastwolfkill.prev;
				while (firstWolf != lastwolfkill) {
					if (!firstWolf.isDead&&firstWolf.getFraction() == Fraction.Wolf) {
						break;
					}
				}
			}
			if (firstWolf.getFraction() == Fraction.Wolf) {
				kill(firstWolf, DiedReason.Burn);
			}
		}
		if (lastwolfkill.isArcherProtected && !lastwolfkill.isGuarded) {
			Villager firstWolf = lastwolfkill.prev;
			while (firstWolf != lastwolfkill) {
				if (!firstWolf.isDead&&firstWolf instanceof Werewolf) {
					break;
				}
			}
			if (firstWolf == lastwolfkill) {
				firstWolf = lastwolfkill.prev;
				while (firstWolf != lastwolfkill) {
					if (!firstWolf.isDead&&firstWolf.getFraction() == Fraction.Wolf) {
						break;
					}
				}
			}
			if (firstWolf.getFraction() == Fraction.Wolf && firstWolf != lastwolfkill) {
				kill(firstWolf, DiedReason.Shoot);
			}
		}
		tokill.removeIf(in -> in.shouldSurvive());
		for (Villager px : tokill) {
			px.isDead = true;
		}
		for (Villager p2 : playerlist) {
			if (p2 instanceof Bear && p2.isDead) {
				sendPublicMessage("昨晚熊没有咆哮。");
			}
			if (p2.isDead) {
				continue;
			}
			p2.onTurn(4);
		}

		Set<Villager> tks = new HashSet<>(tokill);
		tokill.clear();
		boolean shouldWait = false;
		for (Villager p : tks) {
			shouldWait |= p.onDiePending(p.getEffectiveDiedReason());
		}
		if (!shouldWait && tokill.isEmpty() && VictoryPending())
			return;
		startWait(30000, WaitReason.Generic);
		removeAllListeners();
		while (!tokill.isEmpty()) {
			tks.addAll(tokill);
			tokill.clear();
			boolean haswait = false;
			for (Villager p : tks) {
				haswait |= p.onDiePending(p.getEffectiveDiedReason());
			}
			if (haswait) {
				startWait(30000, WaitReason.Generic);
			}
			removeAllListeners();
		}
		tokill.addAll(tks);
		getScheduler().execute(() -> onDayTime());
	}



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
		lastVoteOut = null;
		// muteAll(false);
		logger.logTurn(day, "宣布死者");
		Villager lastdeath=null;
		if (!tokill.isEmpty()) {
			lastDeathCount = 0;
			lastdeath=tokill.iterator().next();
			StringBuilder sb = new StringBuilder("天亮了，昨晚的死者是：\n");
			for (Villager p : tokill) {
				p.isDead = true;
				lastDeathCount++;
				sb.append(p.getNameCard());
				sb.append("\n");
			}
			if (VictoryPending())
				return;
			this.sendPublicMessage(sb.toString());
			for (Villager p : tokill) {
				p.onDied(p.getEffectiveDiedReason());
				logger.logDeath(p, p.getEffectiveDiedReason());
			}
		} else {
			this.sendPublicMessage("昨夜无死者。");
		}
		this.sendPublicMessage(Utils.sendTextAsImage(getAliveList(), getGroup()));
		tokill.clear();
		isFirstNight = false;
		int aliv = 0;
		int tot = 0;
		for (Villager p : playerlist) {
			tot++;
			if (!p.isDead) {
				aliv++;
			}
		}
		this.sendPublicMessage("剩余人数：" + aliv + "/" + tot);
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Villager pb : playerlist) {
			if (pb.isBurned) {
				pb.isBurned = false;
				sendPublicMessage("昨晚，" + pb.getMemberString() + "燃起来了，他的身份是" + pb.getRole() + "。");
			}
		}
		canTalk.clear();
		boolean orderSelected=false;
		for (Villager p : playerlist) {
			if (!p.isDead) {
				p.onTurnStart();
				orderSelected|=p.onSelectOrder(lastdeath);

			}
		}
		if(!orderSelected) {
			canTalk.addAll(playerlist);
		}
		if(isSkippedDay) {
			isSkippedDay=false;
			nextOnDawn();
		}
		isDayTime = true;
		logger.logTurn(day, "白天陈述");
		for (Villager p :canTalk) {
			if (!p.isDead&&!p.isMuted) {
				p.onDayTime();
			}
		}
		vu.skipHalf = true;
		canVote.addAll(playerlist);
		this.sendPublicMessage("你们有15秒思考时间，15秒后开始投票。");
		startWait(15000, WaitReason.Generic);
		// muteAll(true);
		this.sendPublicMessage("请在两分钟内在私聊中完成投票！");
		logger.logTurn(day, "白天投票");
		for (Villager p : playerlist) {
			if (!p.isDead) {
				p.vote();
			}
		}
		if (cursed != null) {
			vu.vote(cursed);
		}
		vu.hintVote(getScheduler());
		canDayVote = true;
		startWait(120000, WaitReason.Vote);
		removeAllListeners();
		if (cursed != null) {
			this.sendPublicMessage(cursed.getMemberString() + "被乌鸦诅咒了。");
		}
		voteKill(vu.getMostVoted());
	}


	public void voteKill(List<Villager> ps) {
		vu.clear();
		if (ps.size() > 1) {
			if (!sameTurn) {
				logger.logTurn(day, "同票PK");
				sameTurn = true;
				this.sendPublicMessage("同票，请做最终陈述。");
				Message mcb = new Message();
				mcb.append("开始投票，请在两分钟内投给以下人物其中之一：\n");
				// muteAll(false);
				canVote.addAll(ps);
				for (Villager p : ps) {
					mcb.append(p.getAt());
					mcb.append("\n");
					p.onDayTime();
				}
				mcb.append("请在两分钟内在私聊中完成投票！");
				this.sendPublicMessage(mcb);
				// muteAll(true);
				for (Villager p : playerlist) {
					if (!p.isDead) {
						p.vote();
					}
				}
				if (cursed != null) {
					vu.vote(cursed);
				}
				vu.hintVote(getScheduler());
				getScheduler().execute(() -> {
					startWait(120000, WaitReason.Vote);
					removeAllListeners();
					voteKill(vu.getMostVoted());
				});
				return;
			}
			this.sendPublicMessage("再次同票，跳过回合。");
			ps.clear();
		}
		canVote.clear();
		sameTurn = false;
		if (ps.size() == 0) {
			this.sendPublicMessage("无人出局");
		} else {
			Villager p = ps.get(0);
			lastVoteOut = p;
			kill(p, DiedReason.Vote);
			// muteAll(false);
			while(tokill.size()>0) {
				List<Villager> lv=new ArrayList<>(tokill);
				tokill.clear();
				for (Villager pe : lv) {
					logger.logDeath(pe, pe.getEffectiveDiedReason());
					if (pe.canDeathSkill(pe.getEffectiveDiedReason())) {
						if (pe.shouldWaitDeathSkill()) {
							pe.onDied(pe.getEffectiveDiedReason());
							continue;
						}
						pe.onDieSkill(pe.getEffectiveDiedReason());
					}
					pe.onSheriffSkill();
					pe.isDead = true;
					if (VictoryPending())
						return;
					pe.onDied(pe.getEffectiveDiedReason(), false);
				}
			}
		}
		tokill.clear();
		canDayVote = false;
		nextOnDawn();
	}

	// 结束回合循环
	public boolean VictoryPending() {
		int total = 0;
		int innos = 0;
		int wolfs = 0;
		if (!canDayVote && cursed != null) {
			innos++;
		}
		for (Villager p : playerlist) {
			if (p.isDead) {
				continue;
			}
			total++;
			if (p.getFraction() == Fraction.Wolf) {
				wolfs++;
				continue;
			}
			innos++;
			if (p instanceof Hunter) {
				innos++;
			} else if (p instanceof Witch && ((Witch) p).hasPoison) {
				innos++;
			} else if (p instanceof Knight && ((Knight) p).hasSkill) {
				innos++;
			} else if (p instanceof Idiot && !((Idiot) p).canVote) {
				innos--;
			} else if (p instanceof MiracleArcher&&((MiracleArcher) p).hasArrow) {
				innos++;
			} else if (p instanceof Arsoner&&!((Arsoner) p).isSkillUsed) {
				innos++;
			} else if (p instanceof WolfKiller) {
				innos += 2;
			}
		}
		boolean ends = false;
		String status = null;
		Fraction winfrac = null;
		if (innos == 0 && wolfs > 0) {
			status = "游戏结束！狼人获胜\n";
			ends = true;
			winfrac = Fraction.Wolf;
		} else if (wolfs == 0 && innos > 0) {
			status = "游戏结束！平民获胜\n";
			winfrac = Fraction.Innocent;
			ends = true;
		} else if (total == 0) {
			status = "游戏结束！同归于尽\n";
			ends = true;
		} else if (wolfs >= innos) {
			status = "游戏结束！狼人获胜\n";
			winfrac = Fraction.Wolf;
			ends = true;
		}
		if (ends) {
			logger.title(status);
			GameData gd = null;
			if (doStat && playerlist.size() >= 6) {
				gd = TableGames.db.getGame(getName());
			}
			removeAllListeners();
			StringBuilder mc = new StringBuilder();
			mc.append(status);
			mc.append("游戏身份：");
			List<Villager> winpls = new ArrayList<>();
			for (Villager p : playerlist) {
				mc.append("\n").append(p.getMemberString()).append("的身份为 ").append(p.getRole()).append(" ")
				.append(DiedReason.getString(p.getEffectiveDiedReason()));
				String nc = p.getNameCard();
				if (nc.indexOf('|') != -1) {
					nc = nc.split("\\|")[1];
				}
				p.setNameCard(nc);
				if (gd != null) {
					WerewolfPlayerData wpd = gd.getPlayer(p.getId(), WerewolfPlayerData.class);
					if (wpd.log(p, winfrac, !p.isDead)) {
						winpls.add(p);
					}
					gd.setPlayer(p.getId(), wpd);
				}
				try {
					p.tryUnmute();
				} catch (Throwable t) {
				}
			}
			if (pointpool > 0&&winpls.size()>0) {
				int ppp = pointpool / winpls.size();
				for (Villager p : winpls) {
					TableGames.credit.get(p.getId()).givePT(ppp);
				}

			}
			// muteAll(false);
			mc.append("\n角色评分：").append(winrate);
			try {
				Thread.sleep(10000);// sbtx好像有频率限制，先等他个10秒再说
			} catch (InterruptedException e) {
			}
			this.sendPublicMessage(Utils.sendTextAsImage(mc.toString(), getGroup()));
			logger.sendLog(getGroup());
			doFinalize();

		}
		isEnded = ends;
		return ends;
	}

}
