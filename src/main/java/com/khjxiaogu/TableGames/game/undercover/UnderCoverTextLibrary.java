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
package com.khjxiaogu.TableGames.game.undercover;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class UnderCoverTextLibrary {
	public static class WordPair{
		private final String w1;
		private final String w2;
		public WordPair(String w1, String w2) {
			this.w1 = w1;
			this.w2 = w2;
		}
		public WordPair getReversed() {
			return new WordPair(w2,w1);
		}
		public String getFirst() {
			return w1;
		}
		public String getSecond() {
			return w2;
		}
	}
	static List<WordPair> ps=new ArrayList<>();
	static Random rnd=new Random();
	public static int read(InputStream is) {
		Scanner sc = null;
		UnderCoverTextLibrary.ps.clear();
		try {
			InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
			sc=new Scanner(isr);
			while(sc.hasNextLine()) {
				String[] ss=sc.nextLine().split(",");
				UnderCoverTextLibrary.ps.add(new WordPair(ss[0],ss[1]));
			}
		}finally {
			if(sc!=null) {
				sc.close();
			}
		}
		return UnderCoverTextLibrary.ps.size();
	}
	public static WordPair getRandomPair() {
		WordPair orig=UnderCoverTextLibrary.ps.get(UnderCoverTextLibrary.rnd.nextInt(UnderCoverTextLibrary.ps.size()));
		if(UnderCoverTextLibrary.rnd.nextBoolean())
			return orig.getReversed();
		return orig;
	}
}
