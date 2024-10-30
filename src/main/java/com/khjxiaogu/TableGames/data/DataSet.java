package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.khjxiaogu.TableGames.platform.GlobalMain;

public class DataSet extends AbstractSet<String> {
	protected Connection database;
	String counter;
	String have;
	String haveKey;

	String putValue;
	String removeValue;
	String delAll;
	String allKey;

	public DataSet(File fn, String db) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			GlobalMain.getLogger().error("SQLITE链接失败！");
			return;
		}

		GlobalMain.getLogger().info("正在链接" + db + "数据库...");
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + fn);
			initDB(db);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error(db + "数据库初始化失败！");
		}
	}

	public DataSet(Connection cn, String db) {
		try {
			database = cn;
			GlobalMain.getLogger().info("正在初始化" + db + "数据库...");
			initDB(db);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error(db + "数据库初始化失败！");
		}
	}
	private void initDB(String db) throws SQLException {
		String createPoM = "CREATE TABLE IF NOT EXISTS " + db + " (key   TEXT PRIMARY KEY ON CONFLICT REPLACE);";// 创建表
		counter = "SELECT COUNT(*) FROM " + db;
		have = "SELECT COUNT(*) FROM " + db + " LIMIT 1";
		haveKey = "SELECT COUNT(*) FROM " + db + " WHERE key = ? LIMIT 1";
		putValue = "INSERT INTO " + db + " VALUES (?)";
		removeValue = "DELETE FROM " + db + " WHERE key = ?";
		delAll = "DELETE FROM " + db;
		allKey = "SELECT key FROM " + db;

		database.createStatement().execute(createPoM);
	}
	private int count(String stmt) {
		try (PreparedStatement ps = database.prepareStatement(stmt); ResultSet rs = ps.executeQuery()) {
			if (rs.next())
				return rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private int count(String stmt, Object... params) {
		try (PreparedStatement ps = database.prepareStatement(stmt);) {
			int t = 0;
			for (Object obj : params)
				ps.setObject(++t, obj);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private ResultSet query(String stmt) throws SQLException {
		PreparedStatement ps = database.prepareStatement(stmt);
		return ps.executeQuery();
		
	}
	private boolean update(String stmt) {
		try {
			return database.createStatement().executeUpdate(stmt) > 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean update(String stmt, Object... params) {
		try (PreparedStatement ps = database.prepareStatement(stmt);) {
			int t = 0;
			for (Object obj : params)
				ps.setObject(++t, obj);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public int size() {
		return count(counter);
	}

	public boolean isEmpty() {
		return count(have) == 0;
	}


	public boolean contains(Object key) {
		if(key instanceof String)
			return count(haveKey, key) > 0;
		return false;
	}


	@Override
	public Iterator<String> iterator() {
		try {
			return new AbstractDataMap.BaseIterator(query(allKey)) {
				@Override
				protected void remove(String key) {
					DataSet.this.remove(key);
				}
			};
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Iterator<String>() {

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public String next() {
				return null;
			}

		};
	}

	@Override
	public boolean add(String e) {
		return update(putValue,e);
	}

	@Override
	public boolean remove(Object o) {
		if(o instanceof String)
			return update(removeValue,o);
		return false;
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		if(c.isEmpty())return false;
		if(isEmpty())return false;
		boolean modified=false;
		for(Object o:c) {
			modified|=remove(o);
		}
		return modified;
		
	}
	@Override
	public void clear() {
		update(delAll);
	}

}
