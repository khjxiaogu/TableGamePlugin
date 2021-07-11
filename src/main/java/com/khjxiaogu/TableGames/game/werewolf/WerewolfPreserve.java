package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.PreserveInfo;


public class WerewolfPreserve extends PreserveInfo<WerewolfGame> {

	public WerewolfPreserve(AbstractRoom g) {
		super(g);
	}

	@Override
	protected int getSuitMembers() {
		return 9;
	}

	@Override
	protected int getMinMembers() {
		return 8;
	}

	@Override
	protected int getMaxMembers() {
		return 18;
	}
	static GameCreater<WerewolfGame> gc=new DefaultGameCreater<>(WerewolfGame.class);
	@Override
	protected GameCreater<WerewolfGame> getGameClass() {
		return gc;
	}

	@Override
	protected boolean isAvailableConfig(AbstractUser ar, String item, String set) {
		if(item.equals("vip")) {
			if(GlobalMain.credit.get(ar.getId()).hasItem("狼人杀vip券")) {
				if(set.equals("神")||set.equals("狼")||set.equals("民")) {
					return true;
				}
				ar.sendPrivate("阵营错误，必须为“神”、“狼”、“民”之一");
			}
			ar.sendPrivate("狼人杀vip券不足！");
		}
		return false;
	}

	@Override
	public String getName() {
		return "狼人杀";
	}

}
