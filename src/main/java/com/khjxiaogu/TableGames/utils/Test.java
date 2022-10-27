package com.khjxiaogu.TableGames.utils;

import java.io.IOException;
import java.net.URLEncoder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.khjxiaogu.TableGames.platform.message.Image;

public class Test {

	public Test() {
	}

	public static void main(String[] args) {
		args=new String[] {"##组字","⿰土豆"};
		try {
			JsonObject jo = JsonParser.parseString(FileUtil.readString(FileUtil.fetch("https://zi.tools/api/ids/lookupids/"+URLEncoder.encode(args[1],"UTF-8")))).getAsJsonObject();
		
		JsonElement je=jo.get(args[1]);
		System.out.println(je);
		if(je.isJsonNull())
			System.out.println("Invalid!");
		else {
			JsonObject ch=je.getAsJsonObject();
			String ret=null;
			if(ch.has("kage")) {
				ret=ch.get("kage").getAsString();
			}
			else if(ch.has("lv1")) {
				String rch=ch.get("lv1").getAsJsonObject().get("match_u_list").getAsJsonArray().get(0).getAsString();
				System.out.println(rch);
				
				return;
			}
			if(ret!=null)
				System.out.println("valid");
			else
				System.out.println("Invalid!");
		}
		} catch (JsonSyntaxException | IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Internal Error");
		}
	}

}
