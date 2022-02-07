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
