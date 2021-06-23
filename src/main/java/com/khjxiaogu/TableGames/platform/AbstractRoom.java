package com.khjxiaogu.TableGames.platform;

import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.MessageListener;


public interface AbstractRoom {
	AbstractPlayer getOwner();
	AbstractPlayer get(long id);
	void sendMessage(IMessage msg);
	void sendMessage(String msg);
	Object getInstance();
	void registerListener(Long id, MessageListener ml);
	void releaseListener(long id);
	void setMuteAll(boolean isMute);
	String getHostNameCard();
}
