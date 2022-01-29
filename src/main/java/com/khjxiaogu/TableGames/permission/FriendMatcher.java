package com.khjxiaogu.TableGames.permission;

import java.util.Arrays;
import java.util.List;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public class FriendMatcher implements PermissionMatcher {
	PermissionResult result;
	@Override
	public PermissionResult match(long id, long group, long botid) {
		return PermissionResult.UNSPECIFIED;
	}

	@Override
	public PermissionResult match(AbstractUser u, boolean temp) {
		if(!temp) {
			return result;
		}
		return PermissionResult.UNSPECIFIED;
	}
	public FriendMatcher(PermissionResult result) {
		this.result = result;
	}
	public FriendMatcher(boolean result) {
		this(PermissionResult.valueOf(result));
	}

	@Override
	public List<String> getValue() {
		return Arrays.asList(result.getSymbol()+"friend");
	}
}
