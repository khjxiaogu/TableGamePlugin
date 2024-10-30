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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import love.forte.simbot.ID;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.bot.OriginBotManager;
import love.forte.simbot.definition.Channel;
import love.forte.simbot.definition.Guild;
import love.forte.simbot.definition.GuildMember;


public class SBHumanUser extends SBUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1931093082691482532L;
	private transient GuildMember member;
	private String bid;
	private String gid;
	private String cid;
	private String mid;
	private SBId id;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		aInputStream.defaultReadObject();
		Bot bot=OriginBotManager.INSTANCE.getBot(ID.$(bid));
		Guild guild=bot.getGuild(ID.$(gid));

		group=guild.getChannel(ID.$(cid));
		member=group.getMember(ID.$(mid));
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		bid=group.getBot().getId().toString();
		cid=group.getId().toString();
		gid=group.getGuildId().toString();
		mid=member.getId().toString();
		aOutputStream.defaultWriteObject();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((member == null) ? 0 : member.hashCode());
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
		SBHumanUser other = (SBHumanUser) obj;
		if (member == null) {
			if (other.member != null)
				return false;
		} else if (!member.equals(other.member))
			return false;
		return true;
	}
	public SBHumanUser(GuildMember member,Channel ch) {
		super(ch);
		this.member = member;
		this.id=SBId.of(member.getId());
	}
	@Override
	public void sendPrivate(String str) {
		try {
			KooKAdapter.INSTANCE.sendMessage(member,str);
		}catch(Exception ex) {

		}
	}
	@Override
	public At getAt() {
		return new At(id);
	}
	@Override
	public String getMemberString() {
		return getNameCard();
	}
	String nco=null;
	@Override
	public void setNameCard(String s) {
		nco=s;
		KookMain.api.setNick(group.getGuildId().toString(),member.getId().toString(), s);
	}
	@Override
	public String getNameCard() {
		if(nco==null)
			nco=member.getNickOrUsername();
		return nco;
		
	}
	@Override
	public void tryMute() {
		try {
			KookMain.api.setMute(group.getId().toString(),member.getId().toString());
		} catch (Throwable t) {
		}
	}
	@Override
	public void tryUnmute() {
		try {
			KookMain.api.setUnmute(group.getId().toString(),member.getId().toString());
		} catch (Throwable t) {
		}
	}
	@Override
	public SBId getId() {
		return id;
	}
	@Override
	public void bind(Object obj) {
	}

	@Override
	public void setGame(Game g) {
	}

	@Override
	public void sendPrivate(IMessage msg) {
		
		try {
			KooKAdapter.INSTANCE.sendMessage(group,msg,group.getBot());
		}catch(Exception ex) {
			
		}
	}
	@Override
	public Permission getPermission() {
		if(member.isOwner())
			return Permission.SYSTEM;
		if(member.isAdmin())
			return Permission.ADMIN;
		return Permission.USER;
	}
	@Override
	public boolean isFriend() {
		return true;
	}
	@Override
	public void tryMuteBackend() {
		try {
			KookMain.api.setInvisible("2452937317768836",member.getId().toString());
		}catch(Throwable t){}
	}
	@Override
	public void tryUnmuteBackend() {
		try {
			KookMain.api.setVisible("2452937317768836",member.getId().toString());
		}catch(Throwable t){}
	}
}
