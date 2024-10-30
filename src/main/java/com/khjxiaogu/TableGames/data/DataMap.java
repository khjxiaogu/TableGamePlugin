package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.khjxiaogu.TableGames.platform.GlobalMain;

public class DataMap extends AbstractDataMap implements Map<String, String> {
	protected Connection database;
	String counter;
	String have;
	String haveValue;
	String haveKey;

	String getValue;
	String putValue;
	String removeValue;
	String delAll;
	String allKey;
	String allEntry;
	String pagination;

	public DataMap(File fn, String db) {
		super();
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

	public DataMap(Connection cn, String db) {
		super();
		try {
			database = cn;
			initDB(db);
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().error(db + "数据库初始化失败！");
		}
	}

	private void initDB(String db) throws SQLException {
		String createPoM = "CREATE TABLE IF NOT EXISTS " + db + " (" + "key   TEXT PRIMARY KEY ON CONFLICT REPLACE, " + // k
				"value TEXT" + // v
				");";// 创建表
		counter = "SELECT COUNT(*) FROM " + db;
		have = "SELECT COUNT(*) FROM " + db + " LIMIT 1";
		haveKey = "SELECT COUNT(*) FROM " + db + " WHERE key = ? LIMIT 1";
		haveValue = "SELECT COUNT(*) FROM " + db + " WHERE value = ? LIMIT 1";
		getValue = "SELECT value FROM " + db + " WHERE key = ? LIMIT 1";
		putValue = "INSERT INTO " + db + " VALUES (?,?)";
		removeValue = "DELETE FROM " + db + " WHERE key = ?";
		delAll = "DELETE FROM " + db;
		allKey = "SELECT key FROM " + db;
		allEntry = "SELECT * FROM " + db;
		pagination="SELECT * FROM "+db+" LIMIT ?,?";
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

	private ResultSet query(String stmt, Object... params) throws SQLException {
		PreparedStatement ps = database.prepareStatement(stmt);
		int t = 0;
		for (Object obj : params)
			ps.setObject(++t, obj);
		try (ResultSet rs = ps.executeQuery()) {
			return rs;
		}
		
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

	public boolean containsValue(Object value) {
		return count(haveValue, value) > 0;
	}

	public boolean containsKey(Object key) {
		return count(haveKey, key) > 0;
	}

	public String get(Object key) {
		try (ResultSet rs = query(getValue, key)) {
			if (rs.next())
				return rs.getString(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// Modification Operations

	public String put(String key, String value) {
		String old = get(key);
		update(putValue, key, value);
		return old;
	}
	@Override
	public void putVoid(String key, String value) {
		update(putValue, key, value);
	}

	public String remove(Object key) {
		String old = get(key);
		update(removeValue, key);
		return old;
	}
	@Override
	public void removeVoid(Object key) {
		update(removeValue, key);
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		for (Map.Entry<? extends String, ? extends String> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	public void clear() {
		update(delAll);
	}

	transient Set<String> keySet;
	transient Collection<String> values;
	transient Set<Entry<String, String>> entrySet;

	public Set<String> keySet() {
		Set<String> ks = keySet;
		if (ks == null) {
			ks = new AbstractSet<String>() {
				public Iterator<String> iterator() {
					try {
						return new BaseIterator(query(allKey)) {
							@Override
							protected void remove(String key) {
								DataMap.this.remove(key);
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

				public int size() {
					return DataMap.this.size();
				}

				public boolean isEmpty() {
					return DataMap.this.isEmpty();
				}

				public void clear() {
					DataMap.this.clear();
				}

				public boolean contains(Object k) {
					return DataMap.this.containsKey(k);
				}
			};
			keySet = ks;
		}
		return ks;
	}

	public Collection<String> values() {
		Collection<String> vals = values;
		if (vals == null) {
			vals = new AbstractCollection<String>() {
				public Iterator<String> iterator() {
					try {
						return new BaseIterator(query(allEntry), 1, 2) {
							@Override
							protected void remove(String key) {
								DataMap.this.remove(key);
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

				public int size() {
					return DataMap.this.size();
				}

				public boolean isEmpty() {
					return DataMap.this.isEmpty();
				}

				public void clear() {
					DataMap.this.clear();
				}

				public boolean contains(Object v) {
					return DataMap.this.containsValue(v);
				}
			};
			values = vals;
		}
		return vals;
	}

	public Set<Entry<String, String>> entrySet() {
		Set<Entry<String, String>> ks = entrySet;
		if (ks == null) {
			ks = new AbstractSet<Entry<String, String>>() {
				public Iterator<Entry<String, String>> iterator() {
					try {
						return new ResultSetIterator<Entry<String, String>>(query(allEntry)) {
							@Override
							protected void remove(String key) {
								DataMap.this.remove(key);
							}

							@Override
							protected Entry<String, String> getValue(ResultSet rs) {
								
								try {
									return new SimpleImmutableEntry(getKey(), rs.getString(2));
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								try {
									return new SimpleImmutableEntry(getKey(),null);
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return new SimpleImmutableEntry(null,null);
							}
						};
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return new Iterator<Entry<String, String>>() {

						@Override
						public boolean hasNext() {
							return false;
						}

						@Override
						public Entry<String, String> next() {
							return null;
						}

					};
				}

				public int size() {
					return DataMap.this.size();
				}

				public boolean isEmpty() {
					return DataMap.this.isEmpty();
				}

				public void clear() {
					DataMap.this.clear();
				}

				public boolean contains(Object k) {
					if (k instanceof Entry) {
						Entry<?, ?> ks = (Entry<?, ?>) k;
						Object val = DataMap.this.get(ks.getKey());
						return AbstractDataMap.eq(val, ks.getValue());
					}
					return false;
				}
			};
			entrySet = ks;
		}
		return ks;
	};

	/**
	 * Returns a shallow copy of this {@code AbstractMap} instance: the keys
	 * and values themselves are not cloned.
	 *
	 * @return a shallow copy of this map
	 */
	protected Object clone() throws CloneNotSupportedException {
		DataMap result = (DataMap) super.clone();
		result.keySet = null;
		result.values = null;
		result.entrySet = null;
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((allEntry == null) ? 0 : allEntry.hashCode());
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataMap other = (DataMap) obj;
		if (allEntry == null) {
			if (other.allEntry != null)
				return false;
		} else if (!allEntry.equals(other.allEntry))
			return false;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		return true;
	}

}
