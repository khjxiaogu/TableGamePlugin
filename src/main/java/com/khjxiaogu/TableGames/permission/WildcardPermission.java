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
