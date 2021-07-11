package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;

import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class MiraiRoomMessageEvent implements RoomMessageEvent {
	GroupMessageEvent ev;
	MiraiHumanUser sender;
	public MiraiRoomMessageEvent(GroupMessageEvent event) {
		ev=event;
	}

	@Override
	public AbstractUser getSender() {
		if(sender!=null)return sender;
		return (sender=new MiraiHumanUser((NormalMember) ev.getSender()));
	}

	@Override
	public AbstractRoom getRoom() {
		return MiraiGroup.createInstance(ev.getGroup());
	}

}
