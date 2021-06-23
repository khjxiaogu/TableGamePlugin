package com.khjxiaogu.TableGames;

import java.io.Serializable;
import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.Message;
import com.khjxiaogu.TableGames.platform.mirai.MiraiListenerUtils;
import com.khjxiaogu.TableGames.utils.MessageListener;

public class Player implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8489601792669124710L;
	protected AbstractPlayer member;
	public Player(AbstractPlayer p) {
		member = p;
	}
	public void sendPrivate(String str) {
		member.sendPrivate(str);
	}
	public void sendPublic(String str) {
		member.sendPublic(str);
	}
	public void sendPublic(Message msg) {
		member.sendPublic(msg);
	}
	public IMessage getAt() {
		return member.getAt();
	}
	public String getMemberString() {
		return member.getMemberString();
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	public void registerListener(MessageListener msgc) {
		member.registerListener(msgc);
	}
	public void releaseListener() {
		member.releaseListener();
	}
	public String getNameCard() {
		return member.getNameCard();
	}
	public void tryMute() {
		member.tryMute();
	}
	public void tryUnmute() {
		member.tryUnmute();
	}
	public long getId() {
		return member.getId();
	}
	public final void bind(Object obj) {
		member.bind(obj);
	}
	public void setGame(Game g) {
		member.setGame(g);
	}
}

