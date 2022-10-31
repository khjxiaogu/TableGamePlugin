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
import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.BotUserLogic;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.RoomMessageListener;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import love.forte.simbot.ID;
import love.forte.simbot.bot.OriginBotManager;
import love.forte.simbot.definition.Channel;
import love.forte.simbot.definition.GuildMember;



public class SBChannel implements AbstractRoom,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<ID,SBChannel> cache=new HashMap<>();
	private String RobotId;
	private String guildId;
	private String groupId;
	transient private Channel group;
	private SBChannel(Channel group) {
		this.group=group;
	}
	@Override
	public String toString() {
		return "SimbotGroup(" + group.getBot().getId() + "@" + group.getGuildId()+"." + group.getId() + ")";
		
	}
	public static SBChannel createInstance(Channel g) {
		SBChannel mg=cache.get(g.getId());
		if(mg!=null)return mg;
		mg=new SBChannel(g);
		
		cache.put(g.getId(),mg);
		return mg;
	}
	@Override
	public int hashCode() {
		return group.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SBChannel other = (SBChannel) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		return true;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		// perform the default de-serialization first
		aInputStream.defaultReadObject();

		group=OriginBotManager.INSTANCE.getBot(ID.$(RobotId)).getGuild(ID.$(guildId)).getChannel(ID.$(groupId));
		// make defensive copy of the mutable Date field
		// ensure that object state has not been corrupted or tampered with malicious code
		//validateUserInfo();
	}

	/**
	 * This is the default implementation of writeObject. Customize as necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

		//ensure that object is in desired state. Possibly run any business rules if applicable.
		//checkUserInfo();
		groupId=group.getId().toString();
		guildId=group.getGuildId().toString();
		RobotId=group.getBot().getId().toString();
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();

	}

	@Override
	public AbstractUser getOwner() {
		return new SBHumanUser(group.getOwner(),group);
	}

	@Override
	public AbstractUser get(UserIdentifier id) {
		if(id instanceof SBId) {
			ID id2=((SBId) id).getIdX();
			GuildMember member=group.getMember(id2);
			if(member!=null)
				return new SBHumanUser(member,group);
		}
		return null;
	}

	@Override
	public void sendMessage(IMessage msg) {
		KooKAdapter.INSTANCE.sendMessage(group,msg,group.getBot());
	}

	@Override
	public void sendMessage(String msg) {
		KooKAdapter.INSTANCE.sendMessage(group,msg);
	}

	@Override
	public Object getInstance() {
		return group;
	}
	@Override
	public void registerRoomListener(Object game,RoomMessageListener ml) {
		SBListenerUtils.registerListener(game,group, ml);
	}
	@Override
	public void releaseRoomListener(Object game) {
		SBListenerUtils.releaseListener(game);
	}
	@Override
	public void registerListener(UserIdentifier id, MessageListener ml) {
		if(id instanceof SBId)
			SBListenerUtils.registerListener(((SBId) id).getIdX(), group, ml);
	}

	@Override
	public void releaseListener(UserIdentifier id) {
		if(id instanceof SBId)
			SBListenerUtils.releaseListener(((SBId) id).getIdX());
	}

	@Override
	public void setMuteAll(boolean isMute) {
		if(isMute)
			KookMain.api.setAllMute(group.getId().toString());
		else
			KookMain.api.setAllUnmute(group.getId().toString());
	}
	String hnc;
	@Override
	public String getHostNameCard() {
		if(hnc==null)
			hnc=KookMain.api.getNick(group.getGuildId().toString(),group.getBot().toMember().getId().toString());
		return hnc;
	}
	@Override
	public SBId getId() {
		return SBId.of(group.getId());
	}
	@Override
	public AbstractBotUser createBot(int id, Class<? extends BotUserLogic> logicCls, Game in) {
		return new SBBotUser(id,this,logicCls,in);
	}
}
