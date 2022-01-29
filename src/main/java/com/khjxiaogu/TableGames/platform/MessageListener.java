package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessageCompound;

@FunctionalInterface
public interface MessageListener{
	void handle(IMessageCompound msg,MsgType type);
}