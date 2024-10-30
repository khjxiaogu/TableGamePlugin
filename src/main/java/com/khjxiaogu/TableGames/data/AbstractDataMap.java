package com.khjxiaogu.TableGames.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractDataMap {

	public abstract void removeVoid(Object key);

	public abstract void putVoid(String key, String value);

	static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}

	public static abstract class BaseIterator extends ResultSetIterator<String> {
		int valueIndex=1;
	
		public BaseIterator(ResultSet rs) {
			super(rs);
		}
	
		public BaseIterator(ResultSet rs, int keyIndex, int valueIndex) {
			super(rs, keyIndex);
			this.valueIndex = valueIndex;
		}
	
		@Override
		protected String getValue(ResultSet rs) {
			try {
				return rs.getString(valueIndex);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	
	}

	public static class SimpleImmutableEntry implements Entry<String, String>, java.io.Serializable {
		private static final long serialVersionUID = -3478940539288466383L;
		private final String key;
		private final String value;
	
		public SimpleImmutableEntry(String key, String value) {
			this.key = key;
			this.value = value;
		}
	
		public SimpleImmutableEntry(Entry<? extends String, ? extends String> entry) {
			this.key = entry.getKey();
			this.value = entry.getValue();
		}
	
		public String getKey() {
			return key;
		}
	
		public String getValue() {
			return value;
		}
	
		public String setValue(String value) {
			throw new UnsupportedOperationException();
		}
	
		public boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
			return eq(key, e.getKey()) && eq(value, e.getValue());
		}
	
		public int hashCode() {
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}
	
		public String toString() {
			return key + "=" + value;
		}
	
	}

	public AbstractDataMap() {
		super();
	}

}