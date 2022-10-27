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
package com.khjxiaogu.TableGames.platform.simplerobot;

import java.io.IOException;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Utils;

import love.forte.simbot.bot.Bot;

public class SBImage extends Image {
	love.forte.simbot.message.Image intern;
	Bot bot;
	boolean original=true;
	public SBImage(love.forte.simbot.message.Image miri,Bot bot) {
		this.intern = miri;
		this.bot=bot;
	}

	@Override
	public byte[] getData() {
		try {
			data=Utils.readAll(intern.getResource().openStream());

			return super.getData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;

	}
	@Override
	public void setData(byte[] data) {
		original=false;
		super.setData(data);
	}

}
