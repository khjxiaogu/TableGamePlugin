package com.khjxiaogu.TableGames.permission;

import java.util.List;

import com.khjxiaogu.TableGames.platform.AbstractUser;

public interface PermissionMatcher {
	PermissionResult match(long id,long group, long botid);
	default PermissionResult match(AbstractUser m) {
		return match(m.getId(),m.getRoom().getId(),m.getHostId());
	}
	/**
	 * @param temp  
	 */
	default PermissionResult match(AbstractUser u,boolean temp) {
		return match(u.getId(),0,u.getHostId());
	}
	List<String> getValue();
}
