package com.khjxiaogu.TableGames.platform;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import com.khjxiaogu.TableGames.utils.Utils;

import kotlin.Pair;

public class Markov {
	String createPoM = "CREATE TABLE IF NOT EXISTS link4 (" + "roll CHAR(3), " + // 用户ID
			"window CHAR(1),"+
			"count LONGINT,"
			+ " PRIMARY KEY(roll,window)" + // 游戏数据json
			") WITHOUT ROWID;";// 创建请求记录表
	Connection database;
	Random def=new Random();
	public static class StateContainer{
		protected String state="";
	}
	public Markov() {
		super();
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			return;
		}
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File("markov.db"));
			database.createStatement().execute(createPoM);
		} catch (Exception e) {
		}
	}

	private final int rank = 3;


	private ArrayList<Pair<String,Long>> getRoll(String roll) {
		try (PreparedStatement ps = database.prepareStatement("SELECT window,count FROM link4 WHERE roll = ?")) {
			ps.setString(1, roll);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					try {
						ArrayList<Pair<String,Long>> gen=new ArrayList<>();
						do {
							gen.add(new Pair<>(rs.getString(1), rs.getLong(2)));
						}while(rs.next());
						//gen.sort(Comparator.comparing(Pair::getFirst));
						return gen;
					}catch(Exception e) {return null;}
				}
			}
		} catch (SQLException e) {
		}
		return null;
	}

	private Pair<Boolean, String> gen(String text,Random rnd) {
		String ans = text;
		String roll = text;
		while (true) {
			ArrayList<Pair<String,Long>> q = getRoll(roll);
			if (q == null)
				return new Pair<>(false, ans);
			long tvalue=q.stream().map(Pair::getSecond).reduce(0L, (i, j) -> i + j);
			long n=0;
			if(tvalue<Integer.MAX_VALUE)
				n=rnd.nextInt((int) tvalue);
			else
				n=rnd.nextInt((int) (tvalue>>32))<<32+rnd.nextInt();
			if(n>tvalue)
				n%=tvalue;
			String c = "";
			for (Pair<String, Long> i : q) {
				n -= i.getSecond();
				if (n <= 0) {
					c = i.getFirst();
					break;
				}
			}
			if ("。？！".contains(c))
				return new Pair<>(true, ans);
			if(c.equals(" ")&&(ans.endsWith(".")||ans.endsWith("?")||ans.endsWith("!")))
				return new Pair<>(true, ans);
			ans += c;
			roll = (roll + c).substring(1);
			if (ans.length() > 100)
				return new Pair<>(false, ans);
		}
	}
	/*public String genMulti(String input) {
		Set<String> poped=new HashSet<>();
		genRolled(input,poped,new HashSet<>(),-1);
		return String.join("\n",poped);
	}
	private int genRolled(String last,Set<String> populate,Set<String> rolling,int seed) {
		Map<String, Long> q = getRoll(last.substring(last.length()-3));
		if (q == null||last.length()>50) {
			populate.add("✗|"+last);
			return q.size();
		}
		int psize=populate.size();
		String c;
		if(rolling.size()<10) {
			if(seed==-1) {
				for (String i : q.keySet()) {
					if(i.equals("。")) {
						populate.add("✓|"+last+i);
						psize=populate.size();
					}else {
						rolling.add(last+i);
					}
					if(rolling.size()+psize>=10)break;
				}
				int ma=1;
				if(rolling.size()+psize<=10) {
					outer:
					for(int i=0;i<ma;i++) {
						Set<String> copy=new HashSet<>(rolling);
						for(String s:copy) {
							ma=Math.max(genRolled(s,populate,rolling,i),ma);
							if(rolling.size()+populate.size()>=10)break outer;
						}
					}
				}
			}else if(seed<q.size()) {
				int i=0;
				for(String o:q.keySet()) {
					if(i==seed) {
						if(o.equals("。")) {
							populate.add("✓|"+last+o);
						}else
							rolling.add(last+o);
						break;
					}
					i++;
				}
			}
		}
		if(seed==-1&&rolling.size()>=10) {
			if(rolling.size()>0) {
				for(String s:rolling) {
					Pair<Boolean, String> g=gen(s.substring(s.length()-3),def);
					populate.add((g.getFirst()?"✓|":"✗|")+g.getSecond());
				}
			}
		}
		return q.size();

	}*/
	public void train(String input,StateContainer sc) {
		try {
			HttpURLConnection huc=(HttpURLConnection) new URL("http://int.khjxiaogu.com/train?input="+URLEncoder.encode(input,"UTF-8").replace("+","%20")+"&State="+URLEncoder.encode(sc.state,"UTF-8").replace("+","%20")).openConnection();
			huc.setDoInput(true);
			huc.connect();
			
			
			sc.state=new String(Utils.readAll(huc.getInputStream()),"UTF-8");
			if(huc.getResponseCode()!=200)
				throw new IOException(huc.getResponseMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public String ret(String input,StateContainer sc) {
		String text = input + "。";
		train(input,sc);
		if (text.length() < 3)
			return null;
		Pair<Boolean, String> g = gen(text.substring(nextInt(def,text.length() - rank)).substring(0, rank),def);
		if (g.getFirst() && g.getSecond().length() > rank) {
			String out=g.getSecond();
			if(input.contains(out))
				return null;
			return out;
		}
		return null;
	}
	public int nextInt(Random rnd,int bound) {
		if(bound==0)return 0;
		return rnd.nextInt(bound);
	}
	public String fret(String input) {
		if(input.length()<3)
			return "too short!";
		Pair<Boolean, String> g = gen(input.substring(nextInt(def,input.length() - rank)).substring(0,rank),def);
		return (g.getFirst()?"✓|":"✗|")+g.getSecond();
	}
	public String sfret(String input,String seed) {
		SecureRandom r;
		try {
			r = SecureRandom.getInstance("SHA1PRNG");
		
        r.setSeed(seed.getBytes(StandardCharsets.UTF_8)); 
		if(input.length()<3)
			return "too short!";
		Pair<Boolean, String> g = gen(input.substring(nextInt(r,input.length() - rank)).substring(0,rank),r);
		return (g.getFirst()?"✓|":"✗|")+g.getSecond();
		} catch (NoSuchAlgorithmException e) {
			return "error!";
		}  
	}
	public String gar(String seed) {
		Random rnd=def;
		try {
			if(seed!=null&&seed.length()>0) {
				SecureRandom r;
				r = SecureRandom.getInstance("SHA1PRNG");
				r.setSeed(seed.getBytes(StandardCharsets.UTF_8)); 
				rnd=r;
			}
			try (PreparedStatement ps = database.prepareStatement("SELECT COUNT(*) FROM link4");ResultSet rs=ps.executeQuery()) {
				rs.next();
				int tcount=rs.getInt(1);
				try (PreparedStatement ps2 = database.prepareStatement("SELECT roll FROM link4 LIMIT 1 OFFSET ?")){
					ps2.setInt(1,rnd.nextInt(tcount));
					try (ResultSet rs2=ps2.executeQuery()){
						if(rs2.next()) {
							Pair<Boolean, String> g = gen(rs2.getString(1),rnd);
							return (g.getFirst()?"✓|":"✗|")+g.getSecond();
						}
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}  
		return "error!";
	}
}
