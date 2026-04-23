package com.khjxiaogu.TableGames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfPlayerData;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.ImagePrintStream;

public class QianghunKill extends Game {
	private static class Player{
		String name;
		double winrate;
		@Override
		public int hashCode() {
			return Objects.hash(name);
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Player other = (Player) obj;
			return Objects.equals(name, other.name);
		}
		public Player(String name, double winrate) {
			super();
			this.name = name;
			this.winrate = winrate;
		}
		@Override
		public String toString() {
			return name;
		}
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<Player> qhau=new ArrayList<>();
	List<Player> nqhau=new ArrayList<>();
	int cpl;
	public QianghunKill(AbstractRoom group, int cplayer) {
		super(group, cplayer, 2);
		cpl=cplayer;
	}

	@Override
	public void forceStart() {
		if(qhau.isEmpty())
			qhau.add(new Player("枪魂",0.26));
		if(nqhau.isEmpty())
			nqhau.add(new Player("淡泊天高",0.329));
		List<Player> qhs=new ArrayList<>(qhau);
		List<Player> nqhs=new ArrayList<>(nqhau);
		Collections.shuffle(nqhs);
		Collections.shuffle(qhs);
		Map<Player,Integer> rank=new HashMap<>();
		for(Player s:nqhs)
			rank.put(s, 0);
		getScheduler().executeLater(()->{
			boolean isWon=false;
			int row=1;
			this.sendPublicMessage("第"+row+"轮");
			while(true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					Thread.currentThread().interrupt();
					break;
				}
				if(qhs.isEmpty()) {
					isWon=true;
					if(qhau.isEmpty())break;
					row++;
					this.sendPublicMessage("第"+row+"轮");
					qhs.addAll(qhau);
					Collections.shuffle(qhs);
				}
				if(nqhs.isEmpty())break;
				Player qh=qhs.remove(0);
				Player nqh=nqhs.remove(0);
				boolean win=Math.random()>0.2;
				this.sendPublicMessage(qh.name+"与"+nqh.name+"决斗，"+(win?nqh.name:qh.name)+"赢了！");
				if(win) {
					rank.merge(nqh, 1, (a,b)->a+b);
					nqhs.add(nqh);
				}else
					qhs.add(qh);
					
			}
			if(!rank.isEmpty()) {
				ImagePrintStream gamelog = new ImagePrintStream();
				for(Entry<Player, Integer> ent:rank.entrySet()) {
					gamelog.println(ent.getKey().name+"打死了"+ent.getValue()+"个枪魂");
				}
				this.sendPublicMessage(new Image(gamelog.asImage()));
			}
			if(isWon) {
				this.sendPublicMessage("游戏结束，枪魂失败！");
			}else{
				this.sendPublicMessage("游戏结束，枪魂胜利！");
			}
			
		},1000);
	}

	@Override
	public String getName() {
		return "枪魂杀";
	}

	@Override
	public boolean isAlive() {
		return false;
	}

	@Override
	public boolean onReAttach(UserIdentifier id) {
		return false;
	}
	@Override
	public boolean addMember(AbstractUser a) {
		
		String memstr=a.getMemberString();
		String idstr=a.getId().getId();
		WerewolfPlayerData v=GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).get(a.getId());
		double wr=0.4;
		if(v!=null)
			wr=v.alive * 1d / (v.alive+v.death);
		if(memstr.contains("枪魂")||memstr.contains("木仓云鬼")||(idstr.startsWith("2019")&&idstr.endsWith("7996"))||(idstr.startsWith("3256")&&idstr.endsWith("9126"))) {
			
			
			qhau.add(new Player(memstr,wr));
		}else {
			nqhau.add(new Player(memstr,wr));
		}
		
		if(qhau.size()+nqhau.size()==cpl) {
			forceStart();
		}
		return true;
	}

}
