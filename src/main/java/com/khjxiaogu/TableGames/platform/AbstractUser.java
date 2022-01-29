package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;


public interface AbstractUser {
	void sendPrivate(String str);
	void sendPrivate(IMessage msg);
	void sendPublic(String str);
	void sendPublic(IMessage msg);
	IMessage getAt();
	String getMemberString();
	void setNameCard(String s);
	String getNameCard();
	AbstractRoom getRoom();
	void tryMute();
	void tryUnmute();
	long getId();
	long getHostId();
	void bind(Object obj);
	void setGame(Game g);
	void registerListener(MessageListener msgc);
	void releaseListener();
	Object getRoleObject();
	Permission getPermission();
}
