package com.khjxiaogu.TableGames.platform.mirai;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.Mirai;

public class MiraiImage extends Image {
	net.mamoe.mirai.message.data.Image miri;
	Bot bot;
	boolean original=true;
	public MiraiImage(net.mamoe.mirai.message.data.Image miri,Bot bot) {
		this.miri = miri;
		this.bot=bot;
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
