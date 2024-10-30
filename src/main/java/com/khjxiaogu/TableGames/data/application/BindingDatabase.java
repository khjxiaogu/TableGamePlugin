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
package com.khjxiaogu.TableGames.data.application;

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
import com.khjxiaogu.TableGames.platform.GlobalMain.BindingTicket;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;

public class BindingDatabase {
	static final String createPoM = "CREATE TABLE IF NOT EXISTS binding (" + "id   TEXT       PRIMARY KEY, " + // 新用户ID
			"user TEXT       NOT NULL" + // 原用户ID
			");";// 创建请求记录表
	static final String createTKT = "CREATE TABLE IF NOT EXISTS tickets (" + "id   TEXT       PRIMARY KEY, " + // 新用户ID
			"user TEXT       NOT NULL," + // 原用户ID
			"token TEXT       NOT NULL" + ");";// 创建请求记录表
	Connection database;

	public BindingDatabase(File datapath) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			GlobalMain.getLogger().error("SQLITE链接失败！");
			return;
		}
		GlobalMain.getLogger().info("正在链接SQLITE信息数据库...");
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File(datapath, "profile.db"));
			database.createStatement().execute(BindingDatabase.createPoM);
			database.createStatement().execute(BindingDatabase.createTKT);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error("信息数据库初始化失败！");
		}
	}

	public UserIdentifier getBinding(UserIdentifier id) {
		try (PreparedStatement ps = database.prepareStatement("SELECT user FROM binding WHERE id = ?")) {
			ps.setString(1, id.serialize());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return UserIdentifierSerializer.read(rs.getString(1));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return id;
	}

	public boolean putBinding(UserIdentifier nid, UserIdentifier oid) {
		try (PreparedStatement ps = database.prepareStatement("REPLACE INTO binding(id,user) VALUES(?,?)")) {
			ps.setString(1, nid.serialize());
			ps.setString(2, oid.serialize());
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public BindingTicket getTicket(UserIdentifier id) {
		try (PreparedStatement ps = database.prepareStatement("SELECT user,token FROM tickets WHERE id = ?")) {
			ps.setString(1, id.serialize());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return new BindingTicket(UserIdentifierSerializer.read(rs.getString(1)), rs.getString(2));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean putTicket(UserIdentifier id, BindingTicket ticket) {

		try (PreparedStatement ps = database.prepareStatement("REPLACE INTO tickets(id,user,token) VALUES(?,?,?)")) {
			ps.setString(1, id.serialize());
			ps.setString(2, ticket.nid.serialize());
			ps.setString(3, ticket.token);
			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	public boolean delTicket(UserIdentifier id) {

		try (PreparedStatement ps = database.prepareStatement("DELETE FROM tickets WHERE id =?")) {
			ps.setString(1, id.serialize());

			return ps.executeUpdate() > 0;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
}
