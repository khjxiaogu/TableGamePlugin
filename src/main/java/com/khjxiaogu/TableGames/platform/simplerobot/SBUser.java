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
package com.khjxiaogu.TableGames.platform.simplerobot;

import java.io.Serializable;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;

import love.forte.simbot.definition.Channel;


public abstract class SBUser implements AbstractUser,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5656814787243115641L;

	transient Channel group;
	transient protected Object roleObject;
	
	@Override
	public void bind(Object obj) {
		roleObject=obj;
	}

	public SBUser(Channel group) {
		this.group = group;
	}

	@Override
	public void sendPublic(String str) {
		try {
			
			KooKAdapter.INSTANCE.sendMessage(group,this.getAt().asMessage().append(str),group.getBot());
		}catch(Exception ex) {
		}
	}

	@Override
	public void sendPublic(IMessage msg) {
		try {
			KooKAdapter.INSTANCE.sendMessage(group,this.getAt().asMessage().append(msg.asMessage()),group.getBot());
		}catch(Exception ex) {
			
		}
	}

	@Override
	public abstract SBId getId();
	@Override
	public Object getRoleObject() {
		return roleObject;
	}
	@Override
	public UserIdentifier getHostId() {
		return SBId.of(group.getBot().getId());
	}
	@Override
	public AbstractRoom getRoom() {
		return SBChannel.createInstance(group);
	}

	@Override
	public void sendForName(String str) {
		try {
			KooKAdapter.INSTANCE.sendMessage(group,this.getMemberString()+" "+str);
		}catch(Exception ex) {

		}
	}

	@Override
	public void sendForName(IMessage msg) {
		try {
			MessageCompound msgx=msg.asMessage();
			msgx.add(0,new Text(getMemberString()));
			KooKAdapter.INSTANCE.sendMessage(group,msgx,group.getBot());
			
		}catch(Exception ex) {
		}
	}
	@Override
	public void tryAvailable() {

	}
}
