package com.khjxiaogu.TableGames.undercover;

import com.khjxiaogu.TableGames.Player;

import net.mamoe.mirai.contact.Member;

public class Innocent extends Player{
	String item;
	boolean isSpy;
	boolean isDead=false;
	public Innocent(Member member,String item,boolean isSpy) {
		super(member);
		this.item=item;
		this.isSpy=isSpy;
	}
	public void onGameStart() {
		super.sendPrivate("你要描述的词语是：“"+item+"”");
	}
}
