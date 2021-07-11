package com.khjxiaogu.TableGames.game.idiomsolitare;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;



public class IdiomLibrary {
	public static Map<String,IdiomInfo> info=new HashMap<>();
	public static int read(InputStream is) {
		Scanner sc = null;
		info.clear();
		try {
			InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
			sc=new Scanner(isr);
			while(sc.hasNextLine()) {
				String[] ss=sc.nextLine().split(",");
				info.put(ss[0],new IdiomInfo(ss[0],ss[1],ss[2]));
			}
		}finally {
			if(sc!=null) {
				sc.close();
			}
		}
		return info.size();
	}
	public static IdiomInfo random() {
		Random r=new Random();
		int i=r.nextInt(info.size());
		for(IdiomInfo ii:info.values()) {
			if(i==0)
				return ii;
			i--;
		}
		return random();
	}
}
