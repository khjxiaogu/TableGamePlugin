/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.permission;

import java.util.function.Predicate;

import com.khjxiaogu.TableGames.platform.Permission;


public enum WildcardPermission {
	admin(m->m==Permission.ADMIN),
	admins(m->m!=Permission.USER),
	owner(m->m==Permission.SYSTEM),
	member(m->m==Permission.USER),
	members(m->true);
	private final Predicate<Permission> matcher;

	private WildcardPermission(Predicate<Permission> matcher) {
		this.matcher = matcher;
	}
	public boolean isMatch(Permission mp) {
		return matcher.test(mp);
	}
}
