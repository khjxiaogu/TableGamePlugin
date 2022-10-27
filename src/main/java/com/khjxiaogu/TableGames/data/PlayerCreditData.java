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
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;

public class PlayerCreditData {

	String createPoM = "CREATE TABLE IF NOT EXISTS profile (" +
			"qq   TEXT PRIMARY KEY ON CONFLICT FAIL, " + // 用户ID
			"data TEXT       NOT NULL DEFAULT '{}' " + // 游戏数据json
			");";// 创建请求记录表
	Connection database;
	public ConcurrentHashMap<UserIdentifier,PlayerCredit> creditCache=new ConcurrentHashMap<>();
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
					
					if(!ac.getValue().hasChange()) {
						ac.getValue().unusedsince++;
						if(ac.getValue().unusedsince>100)
							return true;
						return false;
					}
					ac.getValue().unusedsince=0;
					try(PreparedStatement ps=database.prepareStatement("REPLACE INTO profile(qq,data) VALUES(?,?)")){
						ps.setString(1,GlobalMain.bindings.getBinding(ac.getKey()).serialize());
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
	public PlayerCredit get(UserIdentifier id) {
		PlayerCredit pc=creditCache.get(id);
		if(pc==null) {
			pc=new PlayerCredit();
			try(PreparedStatement ps=database.prepareStatement("SELECT data FROM profile WHERE qq = ?")){
				ps.setString(1,GlobalMain.bindings.getBinding(id).serialize());
				try(ResultSet rs=ps.executeQuery()){
					if(rs.next()){
						pc.load(JsonParser.parseString(rs.getString(1)).getAsJsonObject());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			creditCache.put(id,pc);
		}
		return pc;
	}
}
