package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessageCompound;

@FunctionalInterface
public interface RoomMessageListener{
	void handle(AbstractUser user,IMessageCompound msg,MsgType type);
}