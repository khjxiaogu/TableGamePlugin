package com.khjxiaogu.TableGames.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.ImagePrintStream;

public class QianghunKill extends Game {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<AbstractUser> au=new ArrayList<>();
	int cpl;
	public QianghunKill(AbstractRoom group, int cplayer) {
		super(group, cplayer, 2);
		cpl=cplayer;
	}

	@Override
	public void forceStart() {
		List<AbstractUser> qhs=new ArrayList<>();

		List<AbstractUser> nqhs=new ArrayList<>();
		for(AbstractUser a:au) {
			String memstr=a.getMemberString();
			String idstr=a.getId().getId();
			if(memstr.contains("枪魂")||memstr.contains("木仓云鬼")||(idstr.startsWith("2019")&&idstr.endsWith("7996"))||(idstr.startsWith("3256")&&idstr.endsWith("9126"))) {
				qhs.add(a);
			}else {
				nqhs.add(a);
			}
		}
		List<AbstractUser> bqhs=new ArrayList<>(qhs);
		Collections.shuffle(nqhs);
		Collections.shuffle(qhs);
		Map<AbstractUser,Integer> rank=new HashMap<>();
		getScheduler().executeLater(()->{
			boolean isWon=false;
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
					if(bqhs.isEmpty())break;
					qhs.addAll(bqhs);
					Collections.shuffle(qhs);
				}
				if(nqhs.isEmpty())break;
				AbstractUser qh=qhs.remove(0);
				AbstractUser nqh=nqhs.remove(0);
				boolean win=Math.random()>0.3;
				this.sendPublicMessage(qh.getMemberString()+"与"+nqh.getMemberString()+"决斗，"+(win?nqh.getMemberString():qh.getMemberString())+"赢了！");
				if(win) {
					rank.merge(nqh, 1, (a,b)->a+b);
					nqhs.add(nqh);
				}else
					qhs.add(qh);
					
			}
			if(!rank.isEmpty()) {
				ImagePrintStream gamelog = new ImagePrintStream();
				for(Entry<AbstractUser, Integer> ent:rank.entrySet()) {
					gamelog.println(ent.getKey().getMemberString()+"打死了"+ent.getValue()+"个枪魂");
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
	public boolean addMember(AbstractUser mem) {
		au.add(mem);
		if(au.size()==cpl) {
			forceStart();
		}
		return true;
	}

}
