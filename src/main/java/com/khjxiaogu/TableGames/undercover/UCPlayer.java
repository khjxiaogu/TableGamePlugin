package com.khjxiaogu.TableGames.undercover;

import com.khjxiaogu.TableGames.Player;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary.WordPair;

import net.mamoe.mirai.contact.Member;

public class UCPlayer extends Player{
	boolean isSpy;
	boolean isDead=false;
	public UCPlayer(Member member,boolean isSpy) {
		super(member);
		this.isSpy=isSpy;
	}
	public void onGameStart(WordPair pair) {
		if(isSpy)
			super.sendPrivate("你要描述的词语是：“"+pair.getFirst()+"”");
		else
			super.sendPrivate("你要描述的词语是：“"+pair.getSecond()+"”");
	}
}
