package com.khjxiaogu.TableGames.permission;

import java.util.Arrays;
import java.util.List;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public final class NullOperationMatcher implements PermissionMatcher {
	public static final NullOperationMatcher INSTANCE=new NullOperationMatcher();
	@Override
	public PermissionResult match(AbstractUser m) {
		return PermissionResult.UNSPECIFIED;
	}
	@Override
	public PermissionResult match(AbstractUser u, boolean temp) {
		return PermissionResult.UNSPECIFIED;
	}
	@Override
	public PermissionResult match(long id, long group, long botid) {
		return PermissionResult.UNSPECIFIED;
	}
	@Override
	public List<String> getValue() {
		return Arrays.asList("#");
	}

}
