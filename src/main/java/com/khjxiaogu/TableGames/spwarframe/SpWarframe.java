package com.khjxiaogu.TableGames.spwarframe;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.spwarframe.role.Role;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.WaitThread;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class SpWarframe extends Game implements Platform {
	GameManager gm;
	WaitThread wt=new WaitThread();
	@Override
	public void forceShow(Contact ct) {
		StringBuilder sb=new StringBuilder();
		for(Role r:gm.roles) {
			sb.append("\n").append(r.getPlayer()).append(" ").append(r.getName()).append(" ").append(r.isAlive()?"存活":"死亡");
			if(r.isBoss())
				sb.append(" 是境主");
		}
		sb.append("\n游戏种子：").append(gm.seed);
		ct.sendMessage(sb.toString());
	}

	@Override
	public boolean takeOverMember(long m, Member o) {
		Role r=gm.roles.get((int) m);
		r.bind(new MiraiPlayer(o));
		if(r.isBoss())
			r.getBr().bind(new MiraiPlayer(o));
		return true;
	}

	@Override
	public void forceSkip() {
		wt.stopWait();
	}

	int crc=0;
	public SpWarframe(Group group, int cplayer) {
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
	public boolean addMember(Member mem) {
		synchronized(this) {
			if(crc>=gm.roles.size()) {
				return false;
			}
			MiraiPlayer cur=new MiraiPlayer(mem);
			cur.setNumber(crc);
			Role r=gm.roles.get(crc++);
			r.bind(cur);
			if(r.isBoss())
				r.getBr().bind(cur);
			mem.sendMessage("已经报名");
			if(crc==gm.roles.size())
				super.getScheduler().execute(()->gm.start());
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
	public boolean onReAttach(Long id) {
		return false;
	}

}
