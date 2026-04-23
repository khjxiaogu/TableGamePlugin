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
	List<String> qhau=new ArrayList<>();
	List<String> nqhau=new ArrayList<>();
	int cpl;
	public QianghunKill(AbstractRoom group, int cplayer) {
		super(group, cplayer, 2);
		cpl=cplayer;
	}

	@Override
	public void forceStart() {
		if(qhau.isEmpty())
			qhau.add("枪魂");
		if(nqhau.isEmpty())
			nqhau.add("淡泊天高");
		List<String> qhs=new ArrayList<>(qhau);
		List<String> nqhs=new ArrayList<>(nqhau);
		Collections.shuffle(nqhs);
		Collections.shuffle(qhs);
		Map<String,Integer> rank=new HashMap<>();
		for(String s:nqhs)
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
				String qh=qhs.remove(0);
				String nqh=nqhs.remove(0);
				boolean win=Math.random()>0.2;
				this.sendPublicMessage(qh+"与"+nqh+"决斗，"+(win?nqh:qh)+"赢了！");
				if(win) {
					rank.merge(nqh, 1, (a,b)->a+b);
					nqhs.add(nqh);
				}else
					qhs.add(qh);
					
			}
			if(!rank.isEmpty()) {
				ImagePrintStream gamelog = new ImagePrintStream();
				for(Entry<String, Integer> ent:rank.entrySet()) {
					gamelog.println(ent.getKey()+"打死了"+ent.getValue()+"个枪魂");
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
		if(memstr.contains("枪魂")||memstr.contains("木仓云鬼")||(idstr.startsWith("2019")&&idstr.endsWith("7996"))||(idstr.startsWith("3256")&&idstr.endsWith("9126"))) {
			qhau.add(memstr);
		}else {
			nqhau.add(memstr);
		}
		
		if(qhau.size()+nqhau.size()==cpl) {
			forceStart();
		}
		return true;
	}

}
