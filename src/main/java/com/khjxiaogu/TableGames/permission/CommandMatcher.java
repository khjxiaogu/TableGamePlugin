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
 * aUserIdentifier with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.UserIdentifier;

public class CommandMatcher implements PermissionMatcher {
	PermissionResult wildcard = PermissionResult.UNSPECIFIED;
	LinkedHashMap<String, PermissionMatcher> restricted = new LinkedHashMap<>(5);
	Map<UserIdentifier, GroupMatcher> groupmatchers = new ConcurrentHashMap<>(10);
	Map<UserIdentifier, PermissionResult> friendpermissions = new ConcurrentHashMap<>(10);
	Map<WildcardPermission, GroupMatcher> permmatcher = new LinkedHashMap<>();

	@FunctionalInterface
	interface PermissionFactory {
		PermissionMatcher create(PermissionResult is);

		default PermissionMatcher create(boolean is) {
			return this.create(PermissionResult.valueOf(is));
		};
	}

	@Override
	public PermissionResult match(MatchInfo info) {
		PermissionResult pr = wildcard;
		for (PermissionMatcher pm : restricted.values()) {
			pr=pr.and(pm.match(info));
		}
		pr=pr.and(friendpermissions.getOrDefault(info.caller, PermissionResult.UNSPECIFIED));
		if (info.groupid != null) {
			Permission mp = info.perm;
			for (Entry<WildcardPermission, GroupMatcher> me : permmatcher.entrySet()) {
				if (me.getKey().isMatch(mp)) {
					pr=pr.and(me.getValue().match(info));
				}
			}
			PermissionMatcher pm = groupmatchers.get(info.groupid);
			if (pm != null) {
				pr=pr.and(pm.match(info));
			}
		}
		return pr;
	}


	boolean loadMatcher(String param,UserIdentifier groupe) {
		if (param.length() == 0)
			return false;
		param = param.split("#")[0].trim();
		if (param.length() == 0)
			return false;
		String[] args = param.split("@");
		if (args.length == 1) {
			if(groupe!=null) {
				return groupmatchers.computeIfAbsent(groupe,x->new GroupMatcher()).load(args[0]);
			}
			return loadWildCard(args[0]);
		} else if (args.length == 2) {
			if(groupe!=null)return false;
			if (args[1].equals("*")) {
				
				return loadWildCard(args[0]);
			}
			
			UserIdentifier group = UserIdentifier.parseUserIdentifier(args[1]);
			if(group!=null)
			return groupmatchers.computeIfAbsent(group, x->new GroupMatcher()).load(args[0]);
			
			WildcardPermission wp = WildcardPermission.valueOf(args[1]);
			return permmatcher.computeIfAbsent(wp, x->new GroupMatcher()).load(args[0]);
		}
		return false;
	}

	private boolean loadWildCard(String param) {
		if (param.length() == 0)
			return false;
		char isr = param.charAt(0);
		boolean result = false;
		String s;
		switch (isr) {
		case '+':
			result = true;
			s = param.substring(1);
			break;
		case '-':
			s = param.substring(1);
			break;
		default:
			s = param;
			break;
		}
		
		if (s.charAt(0) == '*') {
			wildcard = PermissionResult.valueOf(result);
			return true;
		}
		UserIdentifier uid=UserIdentifier.parseUserIdentifier(s);
		if (uid!=null) {
			friendpermissions.put(uid, PermissionResult.valueOf(result));
			return true;
		}
		PermissionFactory pf = Matchers.get(s);
		if (pf != null) {
			restricted.put(s, pf.create(result));
			return true;
		}
		return false;
	}

	@Override
	public List<String> getValue() {
		List<String> pl = new ArrayList<>();
		if (wildcard != PermissionResult.UNSPECIFIED)
			pl.add(wildcard.getSymbol() + "*");
		for (PermissionMatcher sp : restricted.values())
			pl.addAll(sp.getValue());
		for (Entry<UserIdentifier, PermissionResult> i : friendpermissions.entrySet()) {
			pl.add(i.getValue().getSymbol() + i.getKey().serialize());
		}
		for (Entry<WildcardPermission, GroupMatcher> i : permmatcher.entrySet()) {
			String gn = "@" + i.getKey().name();
			for (String s : i.getValue().getValue()) {
				pl.add(s + gn);
			}
		}
		for (Entry<UserIdentifier, GroupMatcher> i : groupmatchers.entrySet()) {
			String gn = "@" + i.getKey().serialize();
			for (String s : i.getValue().getValue()) {
				pl.add(s + gn);
			}
		}
		return pl;
	}
}
