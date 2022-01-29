package com.khjxiaogu.TableGames.permission;

import java.util.Arrays;
import java.util.List;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class MemberPermissionMatcher implements PermissionMatcher {
	PermissionResult result;
	WildcardPermission perm;
	@Override
	public PermissionResult match(long id, long group, long botid) {
		//if(group==0)return PermissionResult.UNSPECIFIED;
		return PermissionResult.UNSPECIFIED;
	}

	@Override
	public PermissionResult match(AbstractUser m) {
		return (perm.isMatch(m.getPermission())?result:PermissionResult.UNSPECIFIED);
	}

	@Override
	public PermissionResult match(AbstractUser u, boolean temp) {
		return PermissionResult.UNSPECIFIED;
	}

	public MemberPermissionMatcher(WildcardPermission perm,PermissionResult result) {
		this.perm=perm;
		this.result = result;
	}
	public MemberPermissionMatcher(WildcardPermission perm,boolean result) {
		this(perm,PermissionResult.valueOf(result));
	}

	@Override
	public List<String> getValue() {
		return Arrays.asList(result.getSymbol()+perm.name());
	}
}
