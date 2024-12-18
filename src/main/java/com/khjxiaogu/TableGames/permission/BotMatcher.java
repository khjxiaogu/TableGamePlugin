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

import com.khjxiaogu.TableGames.permission.CommandMatcher.PermissionFactory;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.UserIdentifier;

public class BotMatcher implements PermissionMatcher {
	PermissionResult wildcard = PermissionResult.UNSPECIFIED;
	LinkedHashMap<String, PermissionMatcher> restricted = new LinkedHashMap<>(5);
	Map<String, CommandMatcher> commands = new ConcurrentHashMap<>();
	Map<UserIdentifier, GroupMatcher> groupmatchers = new ConcurrentHashMap<>(10);
	Map<UserIdentifier, PermissionResult> friendpermissions = new ConcurrentHashMap<>(10);
	Map<WildcardPermission, GroupMatcher> permmatcher = new LinkedHashMap<>();

	@Override
	public PermissionResult match(MatchInfo info) {
		if(info.mustMatchCommand) {
			PermissionMatcher pmc = commands.get(info.cmd);
			if(pmc!=null)
				return pmc.match(info);
			return PermissionResult.UNSPECIFIED;
		}
		PermissionResult pr = wildcard;
		
		for (PermissionMatcher pm : restricted.values()) {
			pr = pr.and(pm.match(info));
		}

		pr = pr.and(friendpermissions.getOrDefault(info.caller, PermissionResult.UNSPECIFIED));
		if (info.groupid != null) {
			Permission mp = info.perm;
			for (Entry<WildcardPermission, GroupMatcher> me : permmatcher.entrySet()) {
				if (me.getKey().isMatch(mp)) {
					pr = pr.and(me.getValue().match(info));
				}
			}
			PermissionMatcher pm = groupmatchers.get(info.groupid);
			if (pm != null) {
				pr = pr.and(pm.match(info));
			}
		}
		PermissionMatcher pmc = commands.get(info.cmd);
		if (pmc != null)
			pr = pr.and(pmc.match(info));

		return pr;
	}
	private String[] splitUnescaped(String orig,char c) {
		boolean isEscape=false;
		StringBuilder sb=new StringBuilder();
		List<String> out=new ArrayList<>();
		for(int i=0;i<orig.length();i++) {
			int ch=orig.codePointAt(i);
			if(!isEscape) {
				if(ch=='\\') {
					isEscape=true;
					continue;
				}else if(ch==c) {
					out.add(sb.toString());
					sb=new StringBuilder();
					continue;
				}
					
			}else if(ch!=c&&ch!='\\') {
				sb.append("\\");
			}
			isEscape=false;
			sb.appendCodePoint(ch);
		}
		if(sb.length()>0)
			out.add(sb.toString());
		return out.toArray(new String[0]);
	}
	public boolean loadMatcher(String param,UserIdentifier groupe) {
		if (param.length() == 0)
			return false;
		param = splitUnescaped(param,'#')[0].trim();
		if (param.length() == 0)
			return false;
		String[] cmda = splitUnescaped(param,'$');
		if(cmda[0].equals("*")) {
			cmda=new String[] {cmda[1]};
		}
		if(cmda.length==2) {
			return commands.computeIfAbsent(cmda[0],x->new CommandMatcher()).loadMatcher(cmda[1],groupe);
		}else
		if (cmda.length == 1) {
			String[] args = cmda[0].split("@");

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
					return groupmatchers.computeIfAbsent(group,x->new GroupMatcher()).load(args[0]);
				
				WildcardPermission wp = WildcardPermission.valueOf(args[1]);
				return permmatcher.computeIfAbsent(wp,x->new GroupMatcher()).load(args[0]);
			}
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
		PermissionFactory pf = Matchers.get(s);
		if (pf != null) {
			restricted.put(s, pf.create(result));
			return true;
		}
		UserIdentifier uid=UserIdentifier.parseUserIdentifier(s);
		if(uid!=null) {
			friendpermissions.put(uid, PermissionResult.valueOf(result));
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
		for(Entry<String,CommandMatcher> scm:commands.entrySet()) {
			List<String> lsc=scm.getValue().getValue();
			String gn =  scm.getKey().replaceAll("#","\\#").replaceAll("\\$","\\\\\\$")+"$";
			for (String s : lsc) {
				pl.add(gn+s);
			}
		}
		return pl;
	}
}
