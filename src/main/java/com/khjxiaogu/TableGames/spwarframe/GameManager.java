package com.khjxiaogu.TableGames.spwarframe;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.khjxiaogu.TableGames.spwarframe.Exceptions.RoleNotExistException;
import com.khjxiaogu.TableGames.spwarframe.events.DeadAnnounceEvent;
import com.khjxiaogu.TableGames.spwarframe.events.DiedEvent;
import com.khjxiaogu.TableGames.spwarframe.events.Event;
import com.khjxiaogu.TableGames.spwarframe.events.KillEvent;
import com.khjxiaogu.TableGames.spwarframe.events.RebornEvent;
import com.khjxiaogu.TableGames.spwarframe.events.RevalEvent;
import com.khjxiaogu.TableGames.spwarframe.events.SavedEvent;
import com.khjxiaogu.TableGames.spwarframe.events.SkillEvent;
import com.khjxiaogu.TableGames.spwarframe.events.SystemEvent;
import com.khjxiaogu.TableGames.spwarframe.role.BaiYY;
import com.khjxiaogu.TableGames.spwarframe.role.ChangSQ;
import com.khjxiaogu.TableGames.spwarframe.role.DuanXC;
import com.khjxiaogu.TableGames.spwarframe.role.FeiCY;
import com.khjxiaogu.TableGames.spwarframe.role.GuiHF;
import com.khjxiaogu.TableGames.spwarframe.role.GuoHY;
import com.khjxiaogu.TableGames.spwarframe.role.HeYZ;
import com.khjxiaogu.TableGames.spwarframe.role.JiCR;
import com.khjxiaogu.TableGames.spwarframe.role.JiangTX;
import com.khjxiaogu.TableGames.spwarframe.role.QiLY;
import com.khjxiaogu.TableGames.spwarframe.role.QinMeng;
import com.khjxiaogu.TableGames.spwarframe.role.RanH;
import com.khjxiaogu.TableGames.spwarframe.role.RanXY;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.spwarframe.role.ShenTT;
import com.khjxiaogu.TableGames.spwarframe.role.TangHY;
import com.khjxiaogu.TableGames.spwarframe.role.TongL;
import com.khjxiaogu.TableGames.spwarframe.role.WangXH;
import com.khjxiaogu.TableGames.spwarframe.role.YangYQ;
import com.khjxiaogu.TableGames.spwarframe.role.YaoFX;
import com.khjxiaogu.TableGames.spwarframe.role.YaoFY;
import com.khjxiaogu.TableGames.spwarframe.role.ZhuLY;
import com.khjxiaogu.TableGames.spwarframe.skill.Skill;
import com.khjxiaogu.TableGames.spwarframe.skill.SkillType;

public class GameManager implements EventBus{
	public enum GameTurn{
		DAY(null),
		ATTACK(SkillType.ATTACK),
		SPECIAL(SkillType.SPECIAL),
		INTERRUPT(SkillType.INTERRUPT),
		SAVE(SkillType.SAVE);
		private GameTurn(SkillType available) {
			this.available = available;
		}

		SkillType available;
		public boolean isAvailableFor(SkillType type) {
			return type==available;
		}
	}
	public enum Fraction{
		SNOW("雪"),
		MAPLE("枫"),
		FOG("雾"),
		BOSS("境主"),
		NONE("无");
		private final String name;

		public String getName() {
			return name;
		}

		private Fraction(String name) {
			this.name = name;
		}
	}
	class EventConsumer<T extends Event> implements Consumer<Event>{
		Consumer<T> inner;
		Predicate<Event> accept;
		public EventConsumer(Consumer<T> inner, Predicate<Event> accept) {
			this.inner = inner;
			this.accept = accept;
		}
		@Override
		public void accept(Event t) {
			if(accept.test(t)) {
				inner.accept((T) t);
			}
		}
	}
	Platform p;
	public GameManager(Platform p) {
		this.p=p;
		seed=System.currentTimeMillis();
	}
	public GameManager(Platform p,int seed) {
		this.p=p;

		this.seed=seed;
	}
	public void setMemberCount(int cnt) {
		rnd=new Random(seed);
		roles.clear();
		List<Class<? extends Role>> sl=new ArrayList<>(GameManager.rs.values());


		Collections.shuffle(sl,rnd);
		int boss=rnd.nextInt(cnt);
		for(int i=0;i<cnt;i++) {
			try {
				Role cur=sl.get(i).getConstructor(GameManager.class).newInstance(this);
				if(i<3) {
					cur.setLeader();
				}
				if(i%3==0) {
					cur.setFraction(Fraction.FOG);
				}else if(i%3==1) {
					cur.setFraction(Fraction.SNOW);
				}else {
					cur.setFraction(Fraction.MAPLE);
				}
				roles.add(cur);
				if(i==boss) {
					this.boss=sl.get(i).getConstructor(GameManager.class).newInstance(this);
					this.boss.setBoss();
					cur.setBossNormal(this.boss);
				}
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				i--;
			}
		}
		Collections.shuffle(roles,rnd);
	}
	public void start() {
		StringBuilder sb=new StringBuilder("在场角色：");
		List<String> sl=new ArrayList<>(roles.size());
		for(Role role:roles) {
			sl.add(role.getName());
		}
		Collections.shuffle(sl);
		for(String s:sl) {
			sb.append(s).append(" ");
		}

		for(Role r:roles) {
			if(r.isLeader()) {
				sb.append("\n").append(r.getFraction().getName()).append("：").append(r.getPlayer()).append("角色是").append(r.getName());
			}
			StringBuilder desc=new StringBuilder();
			desc.append("你的角色是：").append(r.getName()).append(" 阵营是：").append(r.getFraction().getName()).append(" 技能列表：");
			for(Skill sk:r.getAllSkills()) {
				desc.append("\n").append(sk.getName()).append("：").append(sk.getDesc()).append(" ");
				if(sk.getType()!=SkillType.PASSIVE) {
					desc.append(sk.getMaxRemain()).append("次");
				}
			}
			if(r.isBoss()) {
				desc.append("\n你是境主");
			}
			r.sendMessage(desc.toString());
		}
		p.sendAll(sb.toString());
		onDayTurn();
		started=true;
	}
	boolean started=true;
	public void sendAll(String message) {p.sendAll(message);}
	public void sendAllLong(String message) {p.sendAllLong(message);}
	LinkedList<Event> fireNext=new LinkedList<>();
	LinkedList<Event> firing=new LinkedList<>();
	boolean eventProceed=false;
	long seed;
	Random rnd;
	Map<Consumer<?>,EventConsumer<?>>listeners=new LinkedHashMap<>();
	Map<Consumer<?>,EventConsumer<?>>hooks=new LinkedHashMap<>();
	static Map<String,Class<? extends Role>> rs=new HashMap<>();
	public static void addRole(Role role) {
		GameManager.rs.put(role.getName(),role.getClass());
	}
	static {
		GameManager dummy=new GameManager(null);
		GameManager.addRole(new BaiYY(dummy));
		GameManager.addRole(new ChangSQ(dummy));
		GameManager.addRole(new DuanXC(dummy));
		GameManager.addRole(new FeiCY(dummy));
		GameManager.addRole(new GuiHF(dummy));
		GameManager.addRole(new GuoHY(dummy));
		GameManager.addRole(new HeYZ(dummy));
		GameManager.addRole(new JiangTX(dummy));
		GameManager.addRole(new JiCR(dummy));
		GameManager.addRole(new QiLY(dummy));
		GameManager.addRole(new QinMeng(dummy));
		GameManager.addRole(new RanH(dummy));
		GameManager.addRole(new RanXY(dummy));
		GameManager.addRole(new ShenTT(dummy));
		GameManager.addRole(new TangHY(dummy));
		GameManager.addRole(new TongL(dummy));
		GameManager.addRole(new WangXH(dummy));
		GameManager.addRole(new YangYQ(dummy));
		GameManager.addRole(new YaoFX(dummy));
		GameManager.addRole(new YaoFY(dummy));
		GameManager.addRole(new ZhuLY(dummy));
	}
	public List<Role> roles=new ArrayList<>();
	Role boss;
	boolean isBossShowup=false;
	public final static long skillwait=60000;
	int turns=0;
	//register event listeners
	@Override
	public <T extends Event> void RegisterListener(Role of,Class<T> ev,Consumer<T> listener) {
		listeners.put(listener,new EventConsumer<>(listener,evt->evt.isTargeting(of)&&ev.isInstance(evt)));
	}
	@Override
	public <T extends Event> void RegisterListener(Class<T> ev,Consumer<T> listener) {
		listeners.put(listener,new EventConsumer<>(listener,evt->ev.isInstance(evt)));
	}
	@Override
	public void RegisterListener(Role of,Consumer<Event> listener) {
		listeners.put(listener,new EventConsumer<>(listener,evt->evt.isTargeting(of)));
	}
	@Override
	public void RegisterListener(Consumer<Event> listener) {
		listeners.put(listener,new EventConsumer<>(listener,evt->true));
	}
	@Override
	public void RemoveListener(Consumer<? extends Event> listener) {
		listeners.remove(listener);
	}
	@Override
	public void CheckEvents(Consumer<Event> checker) {
		for(Event f:firing) {
			checker.accept(f);
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Event> void CheckEvents(Class<T> ev,Consumer<T> checker) {
		for(Event f:firing)
			if(ev.isInstance(f)) {
				checker.accept((T) f);
			}
	}
	@Override
	public void fireEventLater(Event ev) {
		fireNext.add(ev);
	}
	public void doBossShowUp() {
		isBossShowup=true;
		p.sendAll("境主现身！");
	}
	@Override
	public boolean fireEvent(Event ev) {
		for(EventConsumer<?> ec:listeners.values()) {
			ec.accept(ev);
		}
		for(EventConsumer<?> ec:hooks.values()) {
			ec.accept(ev);
		}
		if(ev.isRejected())
			return false;
		firing.add(ev);
		return true;

	}
	public void onDayTurn() {
		firing.removeIf(f->{
			if(f instanceof SystemEvent) {
				if(((SystemEvent) f).toExecute==GameTurn.DAY) {
					f.executeEvent(this);
					return true;
				}
				return false;
			}
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			return false;
		});
		this.HookEvents(RevalEvent.class,ev->{
			if(ev.isRejected()||ev.isCanceled())return;
			ev.cancel();
			ev.executeEvent(this);
		});
		for(Role p:roles) {
			if(p.isAlive()) {
				p.makeSpeak();
				p.askDaySkill();
			}else if(p.isBoss()&&p.getBr().isAlive()) {
				p.getBr().makeSpeak();
				p.getBr().askDaySkill();
			}
		}
		sendAll("现在是白天交流回合，时长5分钟");

		waitForSkill(300000);
		for(Role p:roles) {
			if(p.isAlive()) {
				p.removeListener();
				p.makeMute();
			}else if(p.isBoss()&&p.getBr().isAlive()) {
				p.getBr().removeListener();
				p.getBr().makeMute();
			}
		}
		onAttackSkillTurn();
	}
	public void onAttackSkillTurn() {
		hooks.clear();
		firing.removeIf(f->{
			if(f instanceof SystemEvent) {
				if(((SystemEvent) f).toExecute==GameTurn.ATTACK) {
					f.executeEvent(this);
					return true;
				}
				return false;
			}
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			return false;
		});
		sendAll("现在是杀人技回合");
		for(Role p:roles) {
			if(p.isAlive())
				if(p.askAttackSkill()) {
					waitForSkill(GameManager.skillwait);
					p.removeListener();
					p.sendMessage("您的回合已经结束。");
				}
		}
		if(boss.isAlive())
			if(boss.askAttackSkill()) {
				waitForSkill(GameManager.skillwait);
				boss.removeListener();
				boss.sendMessage("您的回合已经结束。");
			}
		onSpecialSkillTurn();
	}
	public void onSpecialSkillTurn() {

		firing.removeIf(f->{
			if(f instanceof SystemEvent) {
				if(((SystemEvent) f).toExecute==GameTurn.SPECIAL) {
					f.executeEvent(this);
					return true;
				}
				return false;
			}
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			return false;
		});
		sendAll("现在是特殊技回合");
		for(Role p:roles) {
			if(p.isAlive())
				if(p.askSpecialSkill()) {
					waitForSkill(GameManager.skillwait);
					p.removeListener();
					p.sendMessage("您的回合已经结束。");
				}
		}
		if(boss.isAlive())
			if(boss.askSpecialSkill()) {
				waitForSkill(GameManager.skillwait);
				boss.removeListener();
				boss.sendMessage("您的回合已经结束。");
			}
		onInterruptSkillTurn();
	}
	public void onInterruptSkillTurn() {

		Set<Skill> li=new HashSet<>();
		firing.removeIf(f->{
			if(f instanceof SystemEvent) {
				if(((SystemEvent) f).toExecute==GameTurn.INTERRUPT) {
					f.executeEvent(this);
					return true;
				}
				return false;
			}
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof SkillEvent) {
				li.add(((SkillEvent) f).getSkill());
			}
			return false;
		});
		sendAll("现在是干扰技回合");
		ArrayList<Skill> toinpt=new ArrayList<>(li);
		for(Role p:roles) {
			if(p.isAlive())
				if(p.askInterruptSkill(toinpt)) {
					waitForSkill(GameManager.skillwait);
					p.removeListener();
					p.sendMessage("您的回合已经结束。");
				}
		}
		if(boss.isAlive())
			if(boss.askInterruptSkill(toinpt)) {
				waitForSkill(GameManager.skillwait);
				boss.removeListener();
				boss.sendMessage("您的回合已经结束。");
			}
		onSavedSkillTurn();
	}
	public void onSavedSkillTurn() {

		Map<Role,DiedEvent> toDie=new HashMap<>();
		PriorityQueue<Event> pq=new PriorityQueue<>((ev1,ev2)->ev1.getPriority()-ev2.getPriority());
		firing.removeIf(f->{
			if(f instanceof SystemEvent) {
				if(((SystemEvent) f).toExecute==GameTurn.SAVE) {
					f.executeEvent(this);
					return true;
				}
				return false;
			}
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof DiedEvent) {
				toDie.put(((DiedEvent) f).getTarget(),(DiedEvent) f);
				return true;
			}
			if(!(f instanceof SkillEvent))return false;
			pq.add(f);
			return true;
		});
		for(Event f:pq) {
			f.executeEvent(this);
			if(f instanceof KillEvent) {
				KillEvent ke=(KillEvent) f;
				DiedEvent de=toDie.get(ke.getTarget());
				if(de==null) {
					de=new DiedEvent(ke.getTarget());
					toDie.put(ke.getTarget(),de);
				}
				de.populateKill(ke);
			}
		}
		sendAll("现在是救人技回合");
		for(Role p:roles) {
			if(p.isAlive())
				if(p.askSaveSkill(toDie.keySet())) {
					waitForSkill(GameManager.skillwait);
					p.removeListener();
					p.sendMessage("您的回合已经结束。");
				}
		}
		if(boss.isAlive())
			if(boss.askSaveSkill(toDie.keySet())) {
				waitForSkill(GameManager.skillwait);
				boss.removeListener();
				boss.sendMessage("您的回合已经结束。");
			}
		firing.removeIf(f->{
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof RebornEvent&&!((RebornEvent) f).getTarget().isAlive()) {
				((RebornEvent) f).setSaved(new DiedEvent(((RebornEvent) f).getTarget(),((RebornEvent) f).getTarget().lastkill));
				((RebornEvent) f).getTarget().reborn();
				f.executeEvent(this);
				return true;
			}
			if(f instanceof SavedEvent) {
				((SavedEvent) f).setSaved(toDie.remove(((SavedEvent) f).getTarget()));
				f.executeEvent(this);
				return true;
			}
			return false;
		});
		for(DiedEvent de:toDie.values()) {
			fireEvent(de);
		}
		List<DeadAnnounceEvent> daes=new LinkedList<>();
		firing.removeIf(f->{
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof DiedEvent) {

				((DiedEvent) f).getTarget().lastkill=((DiedEvent) f).getKillBy();
				if(((DiedEvent) f).getKillBy().size()==1&&((DiedEvent) f).getKillBy().get(0).getSource()==((DiedEvent) f).getTarget()) {
				}else {
					DeadAnnounceEvent dae=new DeadAnnounceEvent(((DiedEvent) f).getTarget());
					dae.setCounter(((DiedEvent) f).getKillBy().size());
					daes.add(dae);
				}
				f.executeEvent(this);
				return true;
			}
			return false;
		});
		for(DeadAnnounceEvent dae:daes) {
			fireEvent(dae);
			dae.getTarget().kill();
		}

		StringBuilder deadann=new StringBuilder("死亡名单：\n");
		int numd=daes.size();
		Map<Role,DeadAnnounceEvent>prep=new HashMap<>();
		firing.removeIf(f->{
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof DeadAnnounceEvent) {
				if(prep.containsKey(((DeadAnnounceEvent) f).getTarget())) {
					prep.get(((DeadAnnounceEvent) f).getTarget()).addCounter(((DeadAnnounceEvent) f).getCounter());
				}
				prep.put(((DeadAnnounceEvent) f).getTarget(),(DeadAnnounceEvent) f);
				return true;
			}
			return false;
		});
		firing.removeIf(f->{
			if(f.isCanceled())return true;
			if(f.isRejected()) {
				if(f instanceof SkillEvent&&((SkillEvent) f).isMainEvent()) {
					((SkillEvent) f).getSkill().retainRemain();
				}
				return true;
			}
			if(f instanceof DeadAnnounceEvent) {
				deadann.append(((DeadAnnounceEvent) f).getTarget().getName());
				if(((DeadAnnounceEvent) f).getCounter()>1) {
					deadann.append("  属于").append(((DeadAnnounceEvent) f).getTarget().getFraction().getName()).append("境");
					((DeadAnnounceEvent) f).getTarget().exposeFraction();
				}
				deadann.append("\n");
				return true;
			}
			return false;
		});
		if(numd>0) {
			sendAllLong(deadann.toString());
		}
		if(VictoryPending())return;
		turns++;
		onDayTurn();
	}
	public boolean VictoryPending() {
		int mc=0;
		int sc=0;
		int fc=0;
		for(Role r:roles) {
			if(r.isAlive()) {
				switch(r.getFraction()) {
				case FOG:fc++;break;
				case SNOW:sc++;break;
				case MAPLE:mc++;break;
				}
			}
		}
		int tl=(mc==0?1:0)+(sc==0?1:0)+(fc==0?1:0);
		if(tl>=2) {
			if(mc!=0) {
				announceVictory(Fraction.MAPLE);
			} else if(fc!=0) {
				announceVictory(Fraction.FOG);
			} else if(sc!=0) {
				announceVictory(Fraction.SNOW);
			} else {
				announceVictory(null);
			}
			return true;
		}
		if(!isBossShowup&&turns>=8) {
			if(mc>sc) {
				if(mc>fc) {
					announceVictory(Fraction.MAPLE);
					return true;
				}else if(mc<fc) {
					announceVictory(Fraction.FOG);
					return true;
				}
			}else if(mc<sc) {
				if(sc>fc) {
					announceVictory(Fraction.SNOW);
					return true;
				}else if(fc>sc) {
					announceVictory(Fraction.FOG);
					return true;
				}
			}
			if(!boss.isAlive()) {
				announceVictory(boss.lastkill.get(0).getSource().getFraction());
				return true;
			}
			announceVictory(Fraction.NONE);
			return true;
		}
		if(isBossShowup) {
			if(!boss.isAlive()) {
				announceVictory(boss.lastkill.get(0).getSource().getFraction());
				return true;
			}else if(mc+sc+fc==1){
				announceVictory(Fraction.BOSS);
				return true;
			}
		}
		return false;
	}
	public void announceVictory(Fraction f) {
		StringBuilder sb=new StringBuilder(f.getName());
		sb.append("阵营胜利！");
		for(Role r:roles) {
			sb.append("\n").append(r.getPlayer()).append(" ").append(r.getName()).append(" 属于").append(r.getFraction().getName()).append("境").append(" ").append(r.isAlive()?"存活":"死亡");
			if(r.isBoss()) {
				sb.append(" 是境主");
			}
		}
		sb.append("\n游戏种子：").append(seed);
		p.sendAllLong(sb.toString());
		started=false;
	}
	public void fireSystemEventFor(GameTurn turn) {
		firing.removeIf(f->{
			if(f instanceof SystemEvent&&((SystemEvent) f).toExecute==turn) {
				f.executeEvent(this);
				return true;
			}
			return false;
		});
	}
	public void waitForSkill(long time) {
		p.waitTime(time);
	}

	public void skipSkillWait() {
		p.skipWait();
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Event> void HookEvents(Role of, Class<T> ev, Consumer<T> listener) {
		for(Event f:firing)
			if(ev.isInstance(f)&&f.isTargeting(of)) {
				listener.accept((T) f);
			}
		hooks.put(listener,new EventConsumer<>(listener,evt->evt.isTargeting(of)&&ev.isInstance(evt)));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Event> void HookEvents(Class<T> ev, Consumer<T> listener) {
		for(Event f:firing)
			if(ev.isInstance(f)) {
				listener.accept((T) f);
			}
		hooks.put(listener,new EventConsumer<>(listener,evt->ev.isInstance(evt)));
	}

	@Override
	public void HookEvents(Role of, Consumer<Event> listener) {
		for(Event f:firing)
			if(f.isTargeting(of)) {
				listener.accept(f);
			}
		hooks.put(listener,new EventConsumer<>(listener,evt->evt.isTargeting(of)));
	}

	@Override
	public void HookEvents(Consumer<Event> listener) {
		for(Event f:firing) {
			listener.accept(f);
		}
		hooks.put(listener,new EventConsumer<>(listener,evt->true));
	}

	public Role getSkillPlayerByRole(String string) {
		for(Role r:roles) {
			if(r.getName().equals(string))
				return r;
		}
		return null;
	}
	public Role getSkillPlayerByName(String role)throws RoleNotExistException {
		try {
			int r=Integer.parseInt(role);
			return roles.get(r);
		}catch(Exception e) {
			throw new RoleNotExistException(role);
		}

	}
}
