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
package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.QQId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import net.mamoe.mirai.contact.User;

public class MiraiUserFriend implements AbstractUser {
	User member;
	UserIdentifier id;
	public MiraiUserFriend(User sender) {
		this.member=sender;
		id=QQId.of(member.getId());
	}

	@Override
	public void sendPrivate(String str) {
		try {
			SlowUtils.runSlowly(()->member.sendMessage(str));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					member.sendMessage(str);
					return;
				}catch(Exception ex2) {}
			}
		}
	}




	@Override
	public void sendPrivate(IMessage msg) {
		net.mamoe.mirai.message.data.Message pmsg=MiraiAdapter.INSTANCE.toPlatform(msg,member);
		try {
			SlowUtils.runSlowly(()->member.sendMessage(pmsg));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					member.sendMessage(pmsg);
					return;
				}catch(Exception ex2) {}
			}
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
		return member.getNick();
	}

	@Override
	public void setNameCard(String s) {
	}

	@Override
	public String getNameCard() {
		return null;
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
		return QQId.of(member.getBot().getId());
	}
	@Override
	public Permission getPermission() {
		return Permission.USER;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(member.getId());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MiraiUserFriend other = (MiraiUserFriend) obj;
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

	@Override
	public boolean isFriend() {
		return member.getBot().getFriend(member.getId())!=null;
	}
}
