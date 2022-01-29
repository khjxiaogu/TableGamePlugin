package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;

import net.mamoe.mirai.event.events.MessageEvent;

public class MiraiPrivateMessageEvent implements RoomMessageEvent {
	MessageEvent me;
	public MiraiPrivateMessageEvent(MessageEvent event) {
		me=event;
	}

	@Override
	public AbstractUser getSender() {
		return new MiraiUserFriend(me.getSender());
	}

	@Override
	public AbstractRoom getRoom() {
		return null;
	}

}
