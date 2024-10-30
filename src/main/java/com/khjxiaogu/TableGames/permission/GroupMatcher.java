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
import com.khjxiaogu.TableGames.platform.UserIdentifier;

public class GroupMatcher implements PermissionMatcher {
	PermissionResult wildcard=PermissionResult.UNSPECIFIED;
	LinkedHashMap<String,PermissionMatcher> restricted=new LinkedHashMap<>(5);
	Map<UserIdentifier,PermissionResult> memberpermissions=new ConcurrentHashMap<>(10);
	
	@Override
	public PermissionResult match(MatchInfo info) {
		PermissionResult pr=wildcard;
		for(PermissionMatcher sp:restricted.values()) {
			pr=pr.and(sp.match(info));
		}
		pr=pr.and(memberpermissions.getOrDefault(info.caller,PermissionResult.UNSPECIFIED));
		return pr;
	}
	public List<String> getValue(){
		List<String> pl=new ArrayList<>();
		if(wildcard!=PermissionResult.UNSPECIFIED)
			pl.add(wildcard.getSymbol()+"*");
		for(PermissionMatcher sp:restricted.values())
			pl.addAll(sp.getValue());
		for(Entry<UserIdentifier, PermissionResult> i:memberpermissions.entrySet()) {
			pl.add(i.getValue().getSymbol()+i.getKey().serialize());
		}
		return pl;
	}
	boolean load(String param) {
		if(param.length()==0)return false;
		char isr=param.charAt(0);
		boolean result=false;
		String s;
		switch(isr) {
		case '#':return false;
		case '+':result=true;s=param.substring(1);break;
		case '-':s=param.substring(1);break;
		default:s=param;break;
		}
		if(s.charAt(0)=='*') {
			wildcard=PermissionResult.valueOf(result);
			return true;
		}
		PermissionFactory pf=Matchers.get(s);
		if(pf!=null) {
			restricted.put(s,pf.create(result));
			return true;
		}
		UserIdentifier uid=UserIdentifier.parseUserIdentifier(s);
		if(uid!=null) {
			memberpermissions.put(uid,PermissionResult.valueOf(result));
			return true;
		}
		return false;
	}
}
