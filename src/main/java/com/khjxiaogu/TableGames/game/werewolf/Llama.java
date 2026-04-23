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
package com.khjxiaogu.TableGames.game.werewolf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Llama extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public Llama(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
	}

	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}

	@Override
	public String getJobDescription() {
		return "你属于神阵营，从第二晚开始，若你被狼刀，则会在公屏上显示随机一个狼人被喷羊驼了一口。";
	}

	@Override
	public void onTurn() {
		super.StartTurn();
		if(!game.isFirstNight) {
			if(game.lastwolfkill==this) {
				Villager v=getRandomWolf();
				if(v!=null)
					v.isLlamaSplited=true;
			}
		}
	}

	@Override
	public double onVotedAccuracy() {
		return 0.4;
	}

	@Override
	public double onSkilledAccuracy() {
		return 0.5;
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public int getTurn() {
		return 2;
	}

	@Override
	public String getRole() {
		return "羊驼";
	}
}
