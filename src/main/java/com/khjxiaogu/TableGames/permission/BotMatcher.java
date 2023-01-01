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
package com.khjxiaogu.TableGames.permission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;

public class BotMatcher implements PermissionMatcher {
	PermissionResult wildcard = PermissionResult.UNSPECIFIED;
	LinkedHashMap<String, PermissionMatcher> restricted = new LinkedHashMap<>(5);
	Map<UserIdentifier, GroupMatcher> groupmatchers = new ConcurrentHashMap<>(10);
	Map<UserIdentifier, PermissionResult> friendpermissions = new ConcurrentHashMap<>(10);

	@Override
	public PermissionResult match(UserIdentifier id,UserIdentifier group,UserIdentifier botid) {
		PermissionResult pr = wildcard;
		for (PermissionMatcher pm : restricted.values()) {
			pr=pr.and(pm.match(id, group, botid));
		}
		pr=pr.and(friendpermissions.getOrDefault(id, PermissionResult.UNSPECIFIED));
		if (group != null) {
			PermissionMatcher pm = groupmatchers.get(group);
			if (pm != null) {
				pr=pr.and(pm.match(id, group, botid));
			}
		}
		return pr;
	}

	@Override
	public PermissionResult match(AbstractUser m) {
		PermissionResult pr = wildcard;
		for (PermissionMatcher pm : restricted.values()) {
			pr=pr.and(pm.match(m));
		}
		pr=pr.and(friendpermissions.getOrDefault(m.getId(), PermissionResult.UNSPECIFIED));
		//MiraiSongPlugin.getMLogger().info("bgm"+pr.name());
		PermissionMatcher pm = groupmatchers.get(m.getRoom().getId());
		if (pm != null) {
			pr=pr.and(pm.match(m));
			//MiraiSongPlugin.getMLogger().info("gm");
		}
		//GlobalMain.getLogger().info("agm"+pr.name());
		return pr;
	}

	@Override
	public PermissionResult match(AbstractUser m, boolean temp) {
		PermissionResult pr = wildcard;
		for (PermissionMatcher pm : restricted.values()) {
			pr=pr.and(pm.match(m, temp));
		}
		pr=pr.and(friendpermissions.getOrDefault(m.getId(), PermissionResult.UNSPECIFIED));
		return pr;
	}

	void loadMatcher(String param) {
		if (param.length() == 0)
			return;
		param = param.split("#")[0].trim();
		if (param.length() == 0)
			return;
		String[] args = param.split("@");
		if (args.length == 1) {
			loadWildCard(args[0]);
		} else if (args.length == 2) {
			if (args[1].equals("*")) {
				loadWildCard(args[0]);
				return;
			}
			UserIdentifier group = UserIdentifierSerializer.read(args[1]);
			GroupMatcher gm = groupmatchers.get(group);
			if (gm == null) {
				gm = new GroupMatcher();
				groupmatchers.put(group, gm);
			}
			gm.load(args[0]);
		}
	}

	private void loadWildCard(String param) {
		if (param.length() == 0)
			return;
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
		} else {
			PermissionFactory pf = Matchers.get(s);
			if (pf != null)
				restricted.put(s, pf.create(result));
			else
				friendpermissions.put(UserIdentifierSerializer.read(s), PermissionResult.valueOf(result));
		}
		
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
		for (Entry<UserIdentifier, GroupMatcher> i : groupmatchers.entrySet()) {
			String gn = "@" + i.getKey().serialize();
			for (String s : i.getValue().getValue()) {
				pl.add(s + gn);
			}
		}
		return pl;
	}
}
