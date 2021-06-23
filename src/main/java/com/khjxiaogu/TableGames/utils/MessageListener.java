package com.khjxiaogu.TableGames.utils;

import com.khjxiaogu.TableGames.platform.message.Message;

@FunctionalInterface
public interface MessageListener{
	public enum MsgType{
		AT,
		PRIVATE,
		PUBLIC,
	}
	void handle(Message msg,MsgType type);
}