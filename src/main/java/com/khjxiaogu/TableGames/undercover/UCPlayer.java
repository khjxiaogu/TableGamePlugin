package com.khjxiaogu.TableGames.undercover;

import com.khjxiaogu.TableGames.Player;
import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary.WordPair;

public class UCPlayer extends Player{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4840462918144195107L;
	boolean isSpy;
	boolean isDead=false;
	public UCPlayer(AbstractPlayer member,boolean isSpy) {
		super(member);
		this.isSpy=isSpy;
	}
	public void onGameStart(WordPair pair) {
		if(isSpy) {
			super.sendPrivate("你要描述的词语是：“"+pair.getFirst()+"”");
		} else {
			super.sendPrivate("你要描述的词语是：“"+pair.getSecond()+"”");
		}
	}
}
