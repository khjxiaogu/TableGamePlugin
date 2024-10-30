package com.khjxiaogu.TableGames.permission;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.UserIdentifier;


public class MatchInfo {
	UserIdentifier bot;
	UserIdentifier caller;
	Permission perm;
	UserIdentifier groupid;
	String cmd;
	boolean isTemp;
	boolean mustMatchCommand;
	public MatchInfo() {
	}
	public MatchInfo(String cmd,UserIdentifier member,UserIdentifier groupid,Permission perm,UserIdentifier bot) {
		super();
		this.bot = bot;
		this.caller = member;
		this.perm=perm;
		this.groupid = groupid;
		this.cmd = cmd;
	}
	public MatchInfo(String cmd,AbstractUser m) {
		this(cmd,m.getId(),m.getRoom().getId(),m.getPermission(),m.getHostId());
	}

	public MatchInfo(String cmd,AbstractUser u,boolean temp) {
		this(cmd,u.getId(),null,Permission.USER,u.getHostId());
		this.isTemp=temp;
	}
	public MatchInfo mustMatchCommand() {
		mustMatchCommand=true;
		return this;
	}
}
