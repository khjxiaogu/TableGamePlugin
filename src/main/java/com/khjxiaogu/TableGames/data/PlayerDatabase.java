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
import com.khjxiaogu.TableGames.game.undercover.UnderCoverPlayerData;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPlayerData;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;


public class PlayerDatabase {
	Gson gs=new GsonBuilder().enableComplexMapKeySerialization().create();
	public static class GameData{
		Gson gs=new GsonBuilder().enableComplexMapKeySerialization().create();
		public static class PlayerData{
			UserIdentifier id;
			GameData db;
			private PlayerData(UserIdentifier id, GameData db) {
				super();
				this.id = id;
				this.db = db;
			}
			public JsonObject getData() {
				return db.getData(id);
			}
			public boolean setData(JsonObject data) {
				return db.setData(id,data);
			}
		}
		String game;
		PlayerDatabase db;
		private GameData(String game, PlayerDatabase db) {
			this.game = game;
			this.db = db;
		}
		public JsonObject getData(UserIdentifier id) {
			return db.getData(id, game);
		}
		public boolean setData(UserIdentifier id,JsonObject data) {
			return db.setData(id, game, data);
		}
		public boolean setData(UserIdentifier id,String data) {
			return db.setData(id, game, data);
		}
		public PlayerData getPlayer(UserIdentifier id) {
			return new PlayerData(id,this);
		}
		public <T> T getPlayer(UserIdentifier id,Class<T> datacls) {
			return gs.fromJson(db.getData(id, game),datacls);
		}
		public boolean setPlayer(UserIdentifier id,Object datacls) {
			return setData(id,gs.toJson(datacls));
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
			GlobalMain.getLogger().error("SQLITE链接失败！");
			return;
		}
		GlobalMain.getLogger().info("正在链接SQLITE信息数据库...");
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File(datapath, "profile.db"));
			database.createStatement().execute(PlayerDatabase.createPoM);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error("信息数据库初始化失败！");
		}
	}
	public JsonObject getData(UserIdentifier id,String game) {
		try(PreparedStatement ps=database.prepareStatement("SELECT data FROM profile WHERE qq = ? AND game = ?")){
			ps.setString(1,id.getId());
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
	public boolean setData(UserIdentifier id,String game,JsonObject data) {
		return setData(id,game,gs.toJson(data));
	}
	public boolean setData(UserIdentifier id,String game,String data) {
		try(PreparedStatement ps=database.prepareStatement("REPLACE INTO profile(qq,game,data) VALUES(?,?,?)")){
			ps.setString(1,id.getId());
			ps.setString(2,game);
			ps.setString(3,data);
			return ps.executeUpdate()>0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public GenericPlayerData<?> getPlayer(UserIdentifier id,String game) {
		return gs.fromJson(getData(id, game),PlayerDatabase.datacls.get(game));
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
	public <T extends GenericPlayerData<?>> Map<UserIdentifier,T> getDatas(String game,Class<T> dcls) {
		Map<UserIdentifier,T> ll=new HashMap<>();
		try(PreparedStatement ps=database.prepareStatement("SELECT qq,data FROM profile WHERE game = ?")){
			ps.setString(1,game);
			try(ResultSet rs=ps.executeQuery()){
				while(rs.next()) {
					ll.put(UserIdentifierSerializer.read(rs.getString(1)),gs.fromJson(rs.getString(2),dcls));
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ll;
	}
	public GameData getGame(String game) {
		return new GameData(game,this);
	}
}
