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

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.simplerobot.KooKAdapter.SendWrapper;
import com.khjxiaogu.TableGames.platform.simplerobot.KooKAdapter.UserWrapper;
import com.khjxiaogu.TableGames.utils.Game;

import love.forte.simbot.ID;


public class SBUserFriend implements AbstractUser {
	UserIdentifier id;
	SendWrapper sw;
	public SBUserFriend(ID senderid) {
		id=SBId.of(senderid);

		sw=new UserWrapper(id.getId());
		
	}

	@Override
	public void sendPrivate(String str) {
		try {
			sw.sendText(str);
		}catch(Exception ex) {
		
		}
	}




	@Override
	public void sendPrivate(IMessage msg) {
		try {
			KooKAdapter.INSTANCE.sendMessage(sw,msg,null);
		}catch(Exception ex) {

		}
	}

	@Override
	public void sendPublic(String str) {
	}

	@Override
	public void sendPublic(IMessage msg) {
	}

	@Override
	public IMessage getAt() {
		return null;
	}

	@Override
	public String getMemberString() {
		return getNameCard();
	}

	@Override
	public void setNameCard(String s) {
	}

	@Override
	public String getNameCard() {
		return KookMain.api.getName(id.toString());
	}

	@Override
	public void tryMute() {
	}

	@Override
	public void tryUnmute() {
	}

	@Override
	public UserIdentifier getId() {
		return id;
	}

	@Override
	public void bind(Object obj) {
	}

	@Override
	public void setGame(Game g) {
	}

	@Override
	public void registerListener(MessageListener msgc) {
	}

	@Override
	public void releaseListener() {
	}

	@Override
	public Object getRoleObject() {
		return null;
	}
	@Override
	public AbstractRoom getRoom() {
		return null;
	}

	@Override
	public UserIdentifier getHostId() {
		return SBId.of("private");
	}
	@Override
	public Permission getPermission() {
		return Permission.USER;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SBUserFriend other = (SBUserFriend) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public void transferListener(AbstractUser another) {
	}

	@Override
	public void sendForName(String str) {
	}

	@Override
	public void sendForName(IMessage msg) {
	}

	@Override
	public void tryAvailable() {
	}
}
