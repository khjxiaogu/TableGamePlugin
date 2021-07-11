package com.khjxiaogu.TableGames.platform.message;

public interface IMessage {
	public default MessageCompound asMessage() {
		return new MessageCompound().append(this);
	}
}
