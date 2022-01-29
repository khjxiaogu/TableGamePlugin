package com.khjxiaogu.TableGames.game.werewolf;

import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.utils.GameCreater;

public class StandardWerewolfCreater implements GameCreater<WerewolfGame>{
	static Map<String,String> dconfig=new HashMap<>();
	static {
		dconfig.put("屠城","false");
		dconfig.put("板","标准");
		
		dconfig.put("空刀","true");
	}
	@Override
	public WerewolfGame createGame(AbstractRoom group, int count) {
		return new WerewolfGame(group,count,dconfig);
	}

	@Override
	public WerewolfGame createGame(AbstractRoom gp, String... args) {
		return new WerewolfGame(gp,12,dconfig);
	}

	@Override
	public WerewolfGame createGame(AbstractRoom gp, int cplayer, Map<String, String> args) {
		args.putAll(dconfig);
		return new WerewolfGame(gp,cplayer,args);
	}

}
