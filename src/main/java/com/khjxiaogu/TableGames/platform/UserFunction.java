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
package com.khjxiaogu.TableGames.platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Supplier;

import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.utils.Game;

public class UserFunction implements Serializable,Supplier<UserIdentifier>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8489601792669124710L;
	protected AbstractUser member;
	public AbstractUser getMember() {
		return member;
	}
	public void setMember(AbstractUser member) {
		this.member = member;
	}
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		aInputStream.defaultReadObject();
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}
	public UserFunction(AbstractUser p) {
		member = p;
	}
	public void sendPrivate(String str) {
		member.sendPrivate(str);
	}
	public void sendPublic(String str) {
		member.sendPublic(str);
	}
	public void sendPublic(MessageCompound msg) {
		member.sendPublic(msg);
	}
	public void sendForName(String str) {
		member.sendForName(str);
	};
	public void sendForName(IMessage msg) {
		member.sendForName(msg);
		
	};
	public IMessage getAt() {
		return member.getAt();
	}
	public String getMemberString() {
		return member.getMemberString();
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	public void registerListener(MessageListener msgc) {
		member.registerListener(msgc);
	}
	public void releaseListener() {
		member.releaseListener();
	}
	public void transferListener(AbstractUser as) {
		member.transferListener(as);
	}
	public String getNameCard() {
		return member.getNameCard();
	}
	public void tryMute() {
		member.tryMute();
	}
	public void tryUnmute() {
		member.tryUnmute();
	}
	public UserIdentifier getId() {
		return member.getId();
	}
	public final void bind(Object obj) {
		member.bind(obj);
	}
	public void setGame(Game g) {
		member.setGame(g);
	}
	public Object getRoleObject() {
		return member.getRoleObject();
	}
	@Override
	public UserIdentifier get() {
		return member.getId();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((member.getId() == null) ? 0 : member.getId().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserFunction other = (UserFunction) obj;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.getId().equals(other.member.getId()))
			return false;
		return true;
	}
}

