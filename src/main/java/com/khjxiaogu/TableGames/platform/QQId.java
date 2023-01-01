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
package com.khjxiaogu.TableGames.platform;

import java.util.HashMap;
import java.util.Map;

public class QQId implements UserIdentifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	long id;
	private static final Map<Long,QQId> cache=new HashMap<>();
	private QQId(long id) {
		this.id=id;
	}

	@Override
	public String getId() {
		return String.valueOf(getQQId());
	}

	@Override
	public String serialize() {
		return String.valueOf(getQQId());
	}

	@Override
	public boolean isActual() {
		return getQQId()>10000;
	}

	public static QQId of(long id) {
		return cache.computeIfAbsent(id,QQId::new);
	}


	public long getQQId() {
		return id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		QQId other = (QQId) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QQId [id=" + id + "]";
	}

}
