package com.khjxiaogu.TableGames;

import java.io.Serializable;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Message;

public class Player implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8489601792669124710L;
	protected AbstractPlayer member;
	public Player(Member member) {
		this.member = new HumanPlayer(member);
	}
	public Player(AbstractPlayer p) {
		this.member = p;
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
	public Message getAt() {
		return member.getAt();
	}
	public String getMemberString() {
		return member.getMemberString();
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
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

