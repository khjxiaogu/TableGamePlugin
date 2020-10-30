package com.khjxiaogu.TableGames;

import net.mamoe.mirai.message.data.Message;

public interface AbstractPlayer {
	public void sendPrivate(String str);
	public void sendPublic(String str);
	public void sendPublic(Message msg);
	public Message getAt();
	public String getMemberString();
	public void setNameCard(String s);
	public String getNameCard();
	public void tryMute();
	public void tryUnmute();
	public long getId();
}
