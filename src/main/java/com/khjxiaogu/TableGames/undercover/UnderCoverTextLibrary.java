package com.khjxiaogu.TableGames.undercover;

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
		ps.clear();
		try {
			InputStreamReader isr=new InputStreamReader(is,StandardCharsets.UTF_8);
			sc=new Scanner(isr);
			while(sc.hasNextLine()) {
				String[] ss=sc.nextLine().split(",");
				ps.add(new WordPair(ss[0],ss[1]));
			}
		}finally {
			if(sc!=null)
				sc.close();
		}
		return ps.size();
	}
	public static WordPair getRandomPair() {
		WordPair orig=ps.get(rnd.nextInt(ps.size()));
		if(rnd.nextBoolean())
			return orig.getReversed();
		return orig;
	}
}
