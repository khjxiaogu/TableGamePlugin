package com.khjxiaogu.TableGames.utils;

import net.mamoe.mirai.message.data.MessageChain;

@FunctionalInterface
public interface MessageListener{
	public enum MsgType{
		AT,
		PRIVATE,
		PUBLIC,
	}
	public void handle(MessageChain msg,MsgType type);
}