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
package com.khjxiaogu.TableGames.platform.mirai.message;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.Mirai;
import net.mamoe.mirai.message.data.FlashImage;

public class MiraiImage extends Image {
	net.mamoe.mirai.message.data.Image miri;
	Bot bot;
	boolean original=true;
	public MiraiImage(net.mamoe.mirai.message.data.Image miri,Bot bot) {
		this.miri = miri;
		this.bot=bot;
	}

	public MiraiImage(FlashImage msg, Bot b) {
		miri=msg.component1();
		bot=b;
	}

	@Override
	public byte[] getData() {
		String url = Mirai.getInstance().queryImageUrl(bot,miri);
		bot.getLogger().info(url);
		URL uri;
		try {
			uri = new URL(url);
			HttpURLConnection huc=(HttpURLConnection) uri.openConnection();
			huc.setDoInput(true);
			huc.setRequestMethod("GET");
			huc.connect();
			//this.getLogger().info("mir get"+Integer.toString(huc.getResponseCode()));
			data=Utils.readAll(huc.getInputStream());

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
