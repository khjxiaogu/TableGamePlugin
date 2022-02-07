/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
