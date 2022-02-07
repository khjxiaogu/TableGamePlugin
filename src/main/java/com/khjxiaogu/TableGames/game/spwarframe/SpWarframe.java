/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.spwarframe;

import com.khjxiaogu.TableGames.game.spwarframe.role.Role;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.WaitThread;

public class SpWarframe extends Game implements Platform {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8817274353799792732L;
	GameManager gm;
	WaitThread wt=new WaitThread();
	@Override
	public void forceShow(AbstractUser ct) {
		StringBuilder sb=new StringBuilder();
		for(Role r:gm.roles) {
			sb.append("\n").append(r.getPlayer()).append(" ").append(r.getName()).append(" ").append(r.isAlive()?"存活":"死亡");
			if(r.isBoss()) {
				sb.append(" 是境主");
			}
		}
		sb.append("\n游戏种子：").append(gm.seed);
		ct.sendPrivate(sb.toString());
	}

	@Override
	public boolean takeOverMember(long m, AbstractUser o) {
		Role r=gm.roles.get((int) m);
		r.bind(new MiraiPlayer(o));
		if(r.isBoss()) {
			r.getBr().bind(new MiraiPlayer(o));
		}
		return true;
	}

	@Override
	public void forceSkip() {
		wt.stopWait();
	}

	int crc=0;
	public SpWarframe(AbstractRoom group, int cplayer) {
		super(group, cplayer, 2);
		gm=new GameManager(this);
		gm.setMemberCount(cplayer/3*3);
	}

	@Override
	public void waitTime(long time) {
		wt.startWait(time);
	}

	@Override
	public void skipWait() {
		wt.stopWait();
	}

	@Override
	public void sendAll(String s) {
		super.getGroup().sendMessage(s);
	}

	@Override
	public void sendAllLong(String s) {
		super.getGroup().sendMessage(Utils.sendTextAsImage(s,super.getGroup()));
	}

	@Override
	public boolean addMember(AbstractUser mem) {
		synchronized(this) {
			if(crc>=gm.roles.size())
				return false;
			MiraiPlayer cur=new MiraiPlayer(mem);
			cur.setNumber(crc);
			Role r=gm.roles.get(crc++);
			r.bind(cur);
			if(r.isBoss()) {
				r.getBr().bind(cur);
			}
			mem.sendPrivate("已经报名");
			if(crc==gm.roles.size()) {
				super.getScheduler().execute(()->gm.start());
			}
			return true;
		}
	}

	@Override
	public void forceStart() {
		super.getScheduler().execute(()->gm.start());
	}

	@Override
	public String getName() {
		return "SP战纪";
	}

	@Override
	public boolean isAlive() {
		return gm.started;
	}

	@Override
	public boolean onReAttach(UserIdentifier id) {
		return false;
	}

}
