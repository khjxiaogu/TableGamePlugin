package com.khjxiaogu.TableGames.data;

import java.io.File;
import java.sql.Connection;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CachedDataMap implements Map<String, String> {
	Map<String,String> cache=new ConcurrentHashMap<>();
	DataMap data;
	public CachedDataMap(Connection cn, String db) {
		data=new DataMap(cn, db);
		reload();
	}

	public CachedDataMap(File fn, String db) {
		data=new DataMap(fn, db);
		reload();
	}
	public int size() {
		return cache.size();
	}

	public boolean isEmpty() {
		return cache.isEmpty();
	}

	public boolean containsValue(Object value) {
		return cache.containsValue(value);
	}

	public boolean containsKey(Object key) {
		return cache.containsKey(key);
	}

	public String get(Object key) {
		return cache.get(key);
	}

	// Modification Operations

	public String put(String key, String value) {
		
		data.putVoid(key, value);
		return cache.put(key, value);
	}

	public String remove(Object key) {
		data.removeVoid(key);
		return cache.remove(key);
	}

	public void putAll(Map<? extends String, ? extends String> m) {
		for (Map.Entry<? extends String, ? extends String> e : m.entrySet())
			put(e.getKey(), e.getValue());
		cache.putAll(m);
	}

	public void clear() {
		data.clear();
		cache.clear();
	}
	public void reload() {
		for(Entry<String, String> k:data.entrySet()) {
			cache.put(k.getKey(), k.getValue());
		}
	}
	transient Set<String> keySet;
	transient Collection<String> values;
	transient Set<Entry<String, String>> entrySet;

	public Set<String> keySet() {
		Set<String> ks = keySet;
		
		if (ks == null) {
			ks = new AbstractSet<String>() {
				public Iterator<String> iterator() {
					return new Iterator<String>() {
						Iterator<Entry<String, String>> it=entrySet().iterator();
						@Override
						public void remove() {
							it.remove();
						}

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public String next() {
							return it.next().getKey();
						}
					};
				}

				public int size() {
					return CachedDataMap.this.size();
				}

				public boolean isEmpty() {
					return CachedDataMap.this.isEmpty();
				}

				public void clear() {
					CachedDataMap.this.clear();
				}

				public boolean contains(Object k) {
					return CachedDataMap.this.containsKey(k);
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
					return new Iterator<String>() {
						Iterator<Entry<String, String>> it=entrySet().iterator();
						@Override
						public void remove() {
							it.remove();
						}

						@Override
						public boolean hasNext() {
							return it.hasNext();
						}

						@Override
						public String next() {
							return it.next().getValue();
						}
					};
				}

				public int size() {
					return CachedDataMap.this.size();
				}

				public boolean isEmpty() {
					return CachedDataMap.this.isEmpty();
				}

				public void clear() {
					CachedDataMap.this.clear();
				}

				public boolean contains(Object v) {
					return CachedDataMap.this.containsValue(v);
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
					return new Iterator<Entry<String, String>>() {
						String key;
						Iterator<Entry<String, String>> it=cache.entrySet().iterator();
						@Override
						public void remove() {
							it.remove();
							data.remove(key);
						}
						@Override
						public boolean hasNext() {
							return it.hasNext();
						}
						@Override
						public Entry<String, String> next() {
							Entry<String, String> i=it.next();
							key=i.getKey();
							return i;
						}

					};
				}

				public int size() {
					return CachedDataMap.this.size();
				}

				public boolean isEmpty() {
					return CachedDataMap.this.isEmpty();
				}

				public void clear() {
					CachedDataMap.this.clear();
				}

				public boolean contains(Object k) {
					if (k instanceof Entry) {
						Entry<?, ?> ks = (Entry<?, ?>) k;
						Object val = CachedDataMap.this.get(ks.getKey());
						return eq(val, ks.getValue());
					}
					return false;
				}
			};
			entrySet = ks;
		}
		return ks;
	};
	private static boolean eq(Object o1, Object o2) {
		return o1 == null ? o2 == null : o1.equals(o2);
	}


}
