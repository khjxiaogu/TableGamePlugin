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

import java.util.HashSet;
import java.util.Set;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserFunction;

/**
 * 猜数字玩家：每位玩家持有自己独立的答案与已知区间。
 */
public class GuessNumberPlayer extends UserFunction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6852349151200576407L;
	/** 猜中的次数 */
	int pt;
	/** 结算后的积分变化 */
	double ptchange;
	/** 本轮答案 */
	int target;
	/** 根据已猜过的数字缩小后的可行区间 */
	int low = GuessNumberGame.MIN;
	int high = GuessNumberGame.MAX;
	/** 本轮已经猜过的数字，不允许重复猜 */
	final Set<Integer> guessed = new HashSet<>();

	public GuessNumberPlayer(AbstractUser p, int target) {
		super(p);
		newRound(target);
	}

	/**
	 * 开始新一轮：换上新答案并把区间重置为整个范围。
	 */
	void newRound(int target) {
		this.target = target;
		this.low = GuessNumberGame.MIN;
		this.high = GuessNumberGame.MAX;
		this.guessed.clear();
	}
}
