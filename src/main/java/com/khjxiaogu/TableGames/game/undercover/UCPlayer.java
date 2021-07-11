package com.khjxiaogu.TableGames.game.undercover;

import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary.WordPair;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserFunction;

public class UCPlayer extends UserFunction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4840462918144195107L;
	boolean isSpy;
	boolean isDead=false;
	public UCPlayer(AbstractUser member,boolean isSpy) {
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
