package com.khjxiaogu.TableGames.platform;

import java.util.HashMap;
import java.util.Map;

import love.forte.simbot.ID;

public class SBId implements UserIdentifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String idx;
	private static final Map<String,SBId> cache=new HashMap<>();
	public SBId(String id) {
		super();
		idx=id;
	}
	public SBId(ID id) {
		super();
		idx=id.toString();
	}
	@Override
	public String getId() {
		return idx;
	}

	@Override
	public String serialize() {
		return "0sb:"+getId();
	}
	public static SBId of(String id) {
		return cache.computeIfAbsent(id,SBId::new);
	}
	public static SBId of(ID id) {
		return cache.computeIfAbsent(id.toString(),t->new SBId(id));
	}
	public static SBId load(String id) {
		if(id.startsWith("0sb:")) {
			return of(id.substring(4));
		}
		return null;
	}
	@Override
	public boolean isActual() {
		return true;
	}
	public ID getIdX() {
		return ID.$(idx);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((idx == null) ? 0 : idx.hashCode());
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
		SBId other = (SBId) obj;
		if (idx == null) {
			if (other.idx != null)
				return false;
		} else if (!idx.equals(other.idx))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return idx;
	}

}
