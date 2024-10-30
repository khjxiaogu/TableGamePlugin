package com.khjxiaogu.TableGames.data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public abstract class ResultSetIterator<V> implements Iterator<V>,AutoCloseable {

	private ResultSet rs;
	boolean isNext = false, cachedNext = false, removed = false;
	int keyIndex = 1;

	public ResultSetIterator(ResultSet rs, int keyIndex) {
		super();
		this.rs = rs;
		this.keyIndex = keyIndex;
	}

	public ResultSetIterator(ResultSet rs) {
		super();
		this.rs = rs;
	}

	public boolean hasNext() {
		if (isNext)
			return cachedNext;
		isNext = true;
		try {
			cachedNext = rs.next();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cachedNext;
	}

	public V next() {
		if (!isNext)
			try {
				cachedNext = rs.next();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		isNext = false;
		removed = false;
		if (!cachedNext)
			throw new NoSuchElementException();
		return getValue(rs);
	}

	protected abstract V getValue(ResultSet rs);

	protected String getKey() throws SQLException {
		return rs.getString(keyIndex);
	}

	public void remove() {
		if (removed)
			throw new IllegalStateException();
		try {
			remove(getKey());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected abstract void remove(String key);

	protected void finalize() throws Throwable {
		try {
			rs.close();
		} catch (Throwable t) {
		}
		super.finalize();
	}

	@Override
	public void close() throws Exception {
		rs.close();
	}

}