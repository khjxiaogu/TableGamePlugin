package com.khjxiaogu.TableGames;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

public class HumanPlayer implements AbstractPlayer {
	private Member member;
	public HumanPlayer(Member member) {
		this.member = member;
	}
	public void sendPrivate(String str) {
		member.sendMessage(str);
	}
	public void sendPublic(String str) {
		member.getGroup().sendMessage(this.getAt().plus(str));
	}
	public void sendPublic(Message msg) {
		member.getGroup().sendMessage(this.getAt().plus(msg));
	}
	public Message getAt() {
		return new At(member);
	}
	public String getMemberString() {
		return member.getNameCard()+"("+getId()+")";
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	public String getNameCard() {
		return member.getNameCard();
	}
	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}
	public void tryUnmute() {
		try {
			member.unmute();
		} catch (Throwable t) {
		}
	}
	public long getId() {
		return member.getId();
	}
}
