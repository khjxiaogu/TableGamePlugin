package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachedSet extends AbstractSet<String> {
	Set<String> cache=Collections.newSetFromMap(new ConcurrentHashMap<>());
	Set<String> data;
	public CachedSet(Connection cn, String db) {
		data=new DataSet(cn, db);
		reload();
	}

	public CachedSet(File fn, String db) {
		data=new DataSet(fn, db);
		reload();
	}
	public CachedSet(Set<String> set) {
		data=set;
		reload();
	}
	public void reload() {
		Iterator<String> it=data.iterator();
		while(it.hasNext()) {
			String dat=it.next();
			cache.add(dat);
		}
	}
	@Override
	public int size() {
		return cache.size();
	}

	@Override
	public boolean isEmpty() {
		return cache.isEmpty();
	}

	@Override
	public boolean contains(Object key) {
		return cache.contains(key);
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			Iterator<String> it=cache.iterator();
			String key;
			@Override
			public void remove() {
				it.remove();
				CachedSet.this.remove(key);
			}

			@Override
			public boolean hasNext() {
				return it.hasNext();
			}

			@Override
			public String next() {
				key=it.next();
				return key;
			}
		};
	}

	@Override
	public boolean add(String e) {
		return cache.add(e)&&data.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return cache.remove(o)&&data.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return cache.removeAll(c)&&data.removeAll(c);
	}

	@Override
	public void clear() {
		cache.clear();
		data.clear();
	}

}
