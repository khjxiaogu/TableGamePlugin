package com.khjxiaogu.TableGames.game;

import java.util.ArrayList;
import java.util.List;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.Game;

public class QianghunKill extends Game {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<AbstractUser> au=new ArrayList<>();
	int cpl;
	public QianghunKill(AbstractRoom group, int cplayer, int nthread) {
		super(group, cplayer, nthread);
		cpl=cplayer;
	}

	@Override
	public void forceStart() {
		boolean isQh=false;
		for(AbstractUser a:au) {
			String memstr=a.getMemberString();
			String idstr=a.getId().getId();
			if(memstr.contains("枪魂")||memstr.contains("木仓云鬼")||(idstr.startsWith("2019")&&idstr.endsWith("7996"))||(idstr.startsWith("3256")&&idstr.endsWith("9126"))) {
				isQh=true;
				break;
			}
		}
		if(isQh)
			this.sendPublicMessage("游戏结束，枪魂失败！");
		else
			this.sendPublicMessage("游戏结束，枪魂胜利！");
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
