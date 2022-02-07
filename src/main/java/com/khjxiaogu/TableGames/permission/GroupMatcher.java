/**
 * Mirai Song Plugin
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
 * GNU Affero General Public License for more details.
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

public class GroupMatcher implements PermissionMatcher {
	PermissionResult wildcard=PermissionResult.UNSPECIFIED;
	LinkedHashMap<String,PermissionMatcher> restricted=new LinkedHashMap<>(5);
	Map<UserIdentifier,PermissionResult> memberpermissions=new ConcurrentHashMap<>(10);
	
	@Override
	public PermissionResult match(AbstractUser m) {
		PermissionResult pr=wildcard;
		
		for(PermissionMatcher sp:restricted.values()) {
			pr=pr.and(sp.match(m));
		}
		//MiraiSongPlugin.getMLogger().info("brm"+pr.name());
		pr=pr.and(memberpermissions.getOrDefault(m.getId(),PermissionResult.UNSPECIFIED));
		//MiraiSongPlugin.getMLogger().info("arm"+pr.name());
		return pr;
	}
	@Override
	public PermissionResult match(AbstractUser u, boolean temp) {
		return PermissionMatcher.super.match(u, temp);
	}
	@Override
	public PermissionResult match(UserIdentifier id,UserIdentifier group,UserIdentifier botid) {
		PermissionResult pr=wildcard;
		for(PermissionMatcher sp:restricted.values()) {
			pr=pr.and(sp.match(id, group,botid));
		}
		pr=pr.and(memberpermissions.getOrDefault(id,PermissionResult.UNSPECIFIED));
		return pr;
	}
	public List<String> getValue(){
		List<String> pl=new ArrayList<>();
		if(wildcard!=PermissionResult.UNSPECIFIED)
			pl.add(wildcard.getSymbol()+"*");
		for(PermissionMatcher sp:restricted.values())
			pl.addAll(sp.getValue());
		for(Entry<UserIdentifier, PermissionResult> i:memberpermissions.entrySet()) {
			pl.add(i.getValue().getSymbol()+i.getKey().toString());
		}
		return pl;
	}
	void load(String param) {
		if(param.length()==0)return;
		char isr=param.charAt(0);
		if(Character.isDigit(isr)) {
			memberpermissions.put(UserIdentifierSerializer.read(param),PermissionResult.DISALLOW);
		}else {
			boolean result=false;
			String s;
			switch(isr) {
			case '#':return;
			case '+':result=true;s=param.substring(1);break;
			case '-':s=param.substring(1);break;
			default:s=param;break;
			}
			if(Character.isDigit(s.charAt(0))) {
				memberpermissions.put(UserIdentifierSerializer.read(s),PermissionResult.valueOf(result));
			}else if(s.charAt(0)=='*') {
				wildcard=PermissionResult.valueOf(result);
			}else {
				PermissionFactory pf=Matchers.get(s);
				if(pf!=null) {
					restricted.put(s,pf.create(result));
				}
			}
		}
	}
}
