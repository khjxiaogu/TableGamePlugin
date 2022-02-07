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
package com.khjxiaogu.TableGames.game.spwarframe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class MiraiPlayer implements Player {
	AbstractUser intern;
	public MiraiPlayer(AbstractUser mem) {
		intern=mem;
	}
	@Override
	public String getName() {
		return intern.getNameCard();
	}
	@Override
	public void sendMessage(String msg) {
		intern.sendPrivate(msg);
	}
	@Override
	public void listenMessage(Consumer<List<String>> msgc) {
		intern.registerListener((msg,msgtype)->{
			if(msgtype==MsgType.PRIVATE) {
				msgc.accept(Arrays.asList(Utils.getPlainText(msg).split(" ")));
			}
		});
	}
	@Override
	public void removeListener() {
		intern.releaseListener();
	}
	@Override
	public void makeSpeak() {
		intern.tryUnmute();
	}
	@Override
	public void makeMute() {
		intern.tryMute();
	}
	@Override
	public void setNumber(int num) {
		String nc=intern.getNameCard();
		if (nc.indexOf('|') != -1) {
			nc = nc.split("\\|")[1];
		}
		intern.setNameCard(num+ "号 |" + nc);
	}

	@Override
	public void removeNumber() {
		String nc = intern.getNameCard();
		if (nc.indexOf('|') != -1) {
			nc = nc.split("\\|")[1];
		}
		intern.setNameCard(nc);
	}

}
