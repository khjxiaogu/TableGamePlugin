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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.guessnumber;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserFunction;

/**
 * 猜数字玩家：记录本局的猜测次数与猜中情况。
 */
public class GuessNumberPlayer extends UserFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6852349151200576407L;
	/** 猜中次数，本局只有猜中的那个人是1，其他人都还是0 */
	int pt;
	/** 本局一共猜了多少次 */
	int guessCount;
	/** 结算后的积分变化 */
	double ptchange;

	public GuessNumberPlayer(AbstractUser p) {
		super(p);
	}
}
