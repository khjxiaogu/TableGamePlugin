package com.khjxiaogu.TableGames;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

public class Player {
	public final Member member;
	public Player(Member member) {
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
	public At getAt() {
		return new At(member);
	}
	public String getMemberString() {
		return member.getNameCard()+"("+member.getId()+")";
	}
}

