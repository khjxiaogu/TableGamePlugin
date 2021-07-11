package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.platform.GlobalMain;

public class PlayerCreditData {

	String createPoM = "CREATE TABLE IF NOT EXISTS profile (" +
			"qq   TEXT PRIMARY KEY ON CONFLICT FAIL, " + // 用户ID
			"data TEXT       NOT NULL DEFAULT '{}' " + // 游戏数据json
			");";// 创建请求记录表
	Connection database;
	public ConcurrentHashMap<Long,PlayerCredit> creditCache=new ConcurrentHashMap<>();
	public PlayerCreditData(File datapath) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			GlobalMain.getLogger().error("SQLITE链接失败！");
			return;
		}
		GlobalMain.getLogger().info("正在链接SQLITE积分数据库...");
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File(datapath, "credit.db"));
			database.createStatement().execute(createPoM);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error("信息数据库初始化失败！");
		}
		new Thread(()->{
			while(true) {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				creditCache.entrySet().removeIf(ac->{
					ac.getValue().unusedsince++;
					if(!ac.getValue().hasChange()) {
						if(ac.getValue().unusedsince>100)
							return true;
						return false;
					}
					try(PreparedStatement ps=database.prepareStatement("REPLACE INTO profile(qq,data) VALUES(?,?)")){
						ps.setString(1,String.valueOf(ac.getKey()));
						ps.setString(2,ac.getValue().save().toString());
						ps.executeUpdate();
					} catch (Exception e) {
						ac.getValue().assumeChange();
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				});
			}
		}).start();
	}
	public PlayerCredit get(Long qq) {
		PlayerCredit pc=creditCache.get(qq);
		if(pc==null) {
			pc=new PlayerCredit();
			try(PreparedStatement ps=database.prepareStatement("SELECT data FROM profile WHERE qq = ?")){
				ps.setString(1,String.valueOf(qq));
				try(ResultSet rs=ps.executeQuery()){
					if(rs.next()){
						pc.load(JsonParser.parseString(rs.getString(1)).getAsJsonObject());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			creditCache.put(qq,pc);
		}
		return pc;
	}
}
