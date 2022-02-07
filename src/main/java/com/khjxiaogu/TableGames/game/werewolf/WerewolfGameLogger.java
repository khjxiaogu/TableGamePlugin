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
package com.khjxiaogu.TableGames.game.werewolf;

import java.io.Serializable;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.ImagePrintStream;

public class WerewolfGameLogger implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ImagePrintStream gamelog = new ImagePrintStream();

	public WerewolfGameLogger() {
	}

	public void logRaw(String s) {
		gamelog.println(s);
	}

	public void logSkill(Villager from, Villager to, String name) {
		logSkill(from.getNameCard(), to, name);
	}

	public void logSkill(String from, Villager to, String name) {
		gamelog.append(from).append(" ").append(name).append("了 ").append(to.getNameCard()).println();
	}

	public void logVote(Villager from, Villager to) {
		gamelog.append(from.getNameCard()).append(" 投票给").append(to.getNameCard()).println();
	}

	public void logDeath(Villager to, DiedReason dr) {
		gamelog.append(to.getNameCard()).append(" ").append(dr.desc).println();
	}

	public void title(String name) {
		gamelog.append("========").append(name).append("========").println();
	}

	public void logTurn(int day, String name) {
		gamelog.append("========第").append(day).append("天").append(name).append("========").println();
	}

	public void sendLog(AbstractUser ct) {
		ct.sendPrivate(new Image(gamelog.asImage()));
	}

	public void sendLog(AbstractRoom ar) {
		ar.sendMessage(new Image(gamelog.asImage()));
	}
}
