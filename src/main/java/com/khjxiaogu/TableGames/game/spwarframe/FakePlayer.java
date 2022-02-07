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
package com.khjxiaogu.TableGames.game.spwarframe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FakePlayer implements Player {
	GameManager room;
	public FakePlayer(GameManager room) {
		this.room = room;
	}

	@Override
	public String getName() {
		return "无玩家";
	}

	@Override
	public void sendMessage(String msg) {
		System.out.println(msg);
	}

	@Override
	public void listenMessage(Consumer<List<String>> msgc) {
		((SpWarframe) room.p).getScheduler().executeLater(()->
		msgc.accept(Arrays.asList("跳过")),1000);
	}

	@Override
	public void removeListener() {
	}

	@Override
	public void makeSpeak() {
	}

	@Override
	public void makeMute() {
	}

	@Override
	public void setNumber(int num) {
	}

	@Override
	public void removeNumber() {
	}

}
