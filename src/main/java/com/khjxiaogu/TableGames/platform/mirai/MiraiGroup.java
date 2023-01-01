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
import com.khjxiaogu.TableGames.platform.QQId;
import com.khjxiaogu.TableGames.platform.RoomMessageListener;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;

public class MiraiGroup implements AbstractRoom,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<Long,MiraiGroup> cache=new HashMap<>();
	private long RobotId;
	private long groupId;
	transient private Group group;
	private MiraiGroup(Group group) {
		this.group=group;
	}
	@Override
	public String toString() {
		return "MiraiGroup(" + group.getBot().getId() + "@" + group.getId() + ")";
		
	}
	public static MiraiGroup createInstance(Group g) {
		MiraiGroup mg=cache.get(g.getId());
		if(mg!=null)return mg;
		mg=new MiraiGroup(g);
		
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
		MiraiGroup other = (MiraiGroup) obj;
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

		group=Bot.getInstances().get(0).getGroup(groupId);
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
		groupId=group.getId();
		RobotId=group.getBot().getId();
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();

	}

	@Override
	public AbstractUser getOwner() {
		return new MiraiHumanUser(group.getOwner());
	}

	@Override
	public AbstractUser get(UserIdentifier id) {
		if(id instanceof QQId) {
			long qq=((QQId) id).getQQId();
			if(group.contains(qq))
				return new MiraiHumanUser(group.get(qq));
		}
		return null;
	}

	@Override
	public void sendMessage(IMessage msg) {
		SlowUtils.runSlowly(()->group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(msg,this)));
	}

	@Override
	public void sendMessage(String msg) {
		SlowUtils.runSlowly(()->group.sendMessage(msg));
	}

	@Override
	public Object getInstance() {
		return group;
	}

	@Override
	public void setMuteAll(boolean isMute) {
		group.getSettings().setMuteAll(isMute);
	}

	@Override
	public String getHostNameCard() {
		return group.getBotAsMember().getNameCard();
	}
	@Override
	public QQId getId() {
		return QQId.of(group.getId());
	}
	@Override
	public AbstractBotUser createBot(int id, Class<? extends BotUserLogic> logicCls, Game in) {
		return new MiraiBotUser(id,this,logicCls,in);
	}
}
