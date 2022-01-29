package com.khjxiaogu.TableGames.permission;

@FunctionalInterface
interface PermissionFactory {
	PermissionMatcher create(PermissionResult is);

	default PermissionMatcher create(boolean is) {
		return this.create(PermissionResult.valueOf(is));
	};
}