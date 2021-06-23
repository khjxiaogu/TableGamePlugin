package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.TableGames;
import com.khjxiaogu.TableGames.undercover.UnderCoverPlayerData;
import com.khjxiaogu.TableGames.werewolf.WerewolfPlayerData;


public class PlayerDatabase {
	Gson gs=new GsonBuilder().create();
	public static class GameData{
		Gson gs=new GsonBuilder().create();
		public static class PlayerData{
			long qq;
			GameData db;
			private PlayerData(long qq, GameData db) {
				super();
				this.qq = qq;
				this.db = db;
			}
			public JsonObject getData() {
				return db.getData(qq);
			}
			public boolean setData(JsonObject data) {
				return db.setData(qq,data);
			}
		}
		String game;
		PlayerDatabase db;
		private GameData(String game, PlayerDatabase db) {
			this.game = game;
			this.db = db;
		}
		public JsonObject getData(long qq) {
			return db.getData(qq, game);
		}
		public boolean setData(long qq,JsonObject data) {
			return db.setData(qq, game, data);
		}
		public boolean setData(long qq,String data) {
			return db.setData(qq, game, data);
		}
		public PlayerData getPlayer(long qq) {
			return new PlayerData(qq,this);
		}
		public <T> T getPlayer(long qq,Class<T> datacls) {
			return gs.fromJson(db.getData(qq, game),datacls);
		}
		public boolean setPlayer(long qq,Object datacls) {
			return setData(qq,gs.toJson(datacls));
		}
	}
	static final String createPoM = "CREATE TABLE IF NOT EXISTS profile (" +
			"qq   TEXT       NOT NULL, " + // 用户ID
			"game TEXT       NOT NULL, " + // 游戏名称
			"data TEXT       NOT NULL DEFAULT '{}', " + // 游戏数据json
			"PRIMARY KEY (qq,game) ON CONFLICT FAIL" + ");";// 创建请求记录表
	Connection database;
	public static Map<String,Class<? extends GenericPlayerData<?>>> datacls=new HashMap<>();
	static {
		PlayerDatabase.datacls.put("狼人杀",WerewolfPlayerData.class);
		PlayerDatabase.datacls.put("谁是卧底",UnderCoverPlayerData.class);
	}
	public PlayerDatabase(File datapath) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			TableGames.plugin.getLogger().error("SQLITE链接失败！");
			return;
		}
		TableGames.plugin.getLogger().info("正在链接SQLITE信息数据库...");
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File(datapath, "profile.db"));
			database.createStatement().execute(PlayerDatabase.createPoM);
		} catch (Exception e) {
			TableGames.plugin.getLogger().error(e);
			TableGames.plugin.getLogger().error("信息数据库初始化失败！");
		}
	}
	public JsonObject getData(long qq,String game) {
		try(PreparedStatement ps=database.prepareStatement("SELECT data FROM profile WHERE qq = ? AND game = ?")){
			ps.setString(1,String.valueOf(qq));
			ps.setString(2,game);
			try(ResultSet rs=ps.executeQuery()){
				if(rs.next())
					return JsonParser.parseString(rs.getString(1)).getAsJsonObject();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new JsonObject();
	}
	public boolean setData(long qq,String game,JsonObject data) {
		return setData(qq,game,data.toString());
	}
	public boolean setData(long qq,String game,String data) {
		try(PreparedStatement ps=database.prepareStatement("REPLACE INTO profile(qq,game,data) VALUES(?,?,?)")){
			ps.setString(1,String.valueOf(qq));
			ps.setString(2,game);
			ps.setString(3,data);
			return ps.executeUpdate()>0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public GenericPlayerData<?> getPlayer(long qq,String game) {
		return gs.fromJson(getData(qq, game),PlayerDatabase.datacls.get(game));
	}
	public GenericPlayerData<?>[] getPlayers(String game) {
		List<GenericPlayerData<?>> ll=new LinkedList<>();
		Class<? extends GenericPlayerData<?>> dcls=PlayerDatabase.datacls.get(game);
		try(PreparedStatement ps=database.prepareStatement("SELECT data FROM profile WHERE game = ?")){
			ps.setString(1,game);
			try(ResultSet rs=ps.executeQuery()){
				while(rs.next()) {
					ll.add(gs.fromJson(rs.getString(1),dcls));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ll.toArray(new GenericPlayerData<?>[0]);
	}
	public GameData getGame(String game) {
		return new GameData(game,this);
	}
}
