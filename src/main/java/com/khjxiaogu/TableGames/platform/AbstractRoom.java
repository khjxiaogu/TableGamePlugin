package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessage;


public interface AbstractRoom {
	AbstractUser getOwner();
	AbstractUser get(long id);
	void sendMessage(IMessage msg);
	void sendMessage(String msg);
	Object getInstance();
	void registerRoomListener(RoomMessageListener ml);
	void registerListener(Long id, MessageListener ml);
	void releaseListener(long id);
	void setMuteAll(boolean isMute);
	String getHostNameCard();
	long getId();
	void releaseRoomListener();
}
