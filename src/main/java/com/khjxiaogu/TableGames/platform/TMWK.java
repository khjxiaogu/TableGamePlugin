package com.khjxiaogu.TableGames.platform;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.utils.Utils;


public class TMWK {
	public static String checkWiki(String txt) {
		try {
			URL u=new URL("https://wiki.teammoeg.com/api.php?action=query&format=json&list=search&srsearch="+URLEncoder.encode(txt,"UTF-8")+"&srlimit=1&srwhat=title&srinfo=&srprop=&srenablerewrites=1");
			HttpURLConnection huc=(HttpURLConnection) u.openConnection();
			huc.setDoInput(true);
			huc.connect();
			String s=new String(Utils.readAll(huc.getInputStream()),StandardCharsets.UTF_8);
			JsonArray je=JsonParser.parseString(s).getAsJsonObject().get("query").getAsJsonObject().get("search").getAsJsonArray();
			if(je.size()>0) {
				String page=je.get(0).getAsJsonObject().get("title").getAsString();
				StringBuilder out=new StringBuilder("检索到页面：");
				out.append(page).append("\n").append("https://wiki.teammoeg.com/index.php?title=").append(URLEncoder.encode(page,"UTF-8"));
				return out.toString();
			}
			huc.disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "没有相关结果";
	
	}
}
