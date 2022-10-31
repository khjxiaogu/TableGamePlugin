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

import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.BotUserLogic;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.Utils;

import love.forte.simbot.definition.Channel;



public class SBBotUser extends SBUser implements Serializable,AbstractBotUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3890210129608510286L;
	protected int rbid;
	transient Game sg;
	protected String nameCard="机器人";
	private BotUserLogic logic;
	
	public SBBotUser(int botId,Channel in,Game g) {
		super(in);
		rbid=botId;
		nameCard="机器人"+rbid;
		sg=g;
	}

	public SBBotUser(int botId,AbstractRoom group,Class<? extends BotUserLogic> logicType, Game in) {
		this(botId,(Channel) group.getInstance(),in);
		logic=Utils.createLogic(logicType,this,in);
	}

	@Override
	public void sendPrivate(String str) {
		GlobalMain.getLogger().debug(str);
		onPrivate(str);
	}

	@Override
	public void sendPublic(String str) {
		sg.getScheduler().executeLater(()->{
			onPublic(str);
		},500);
		GlobalMain.getLogger().debug(str);
		super.sendPublic(str);
	}

	@Override
	public void sendPublic(IMessage str) {
		sg.getScheduler().executeLater(()->{
			if(str instanceof MessageCompound) {
				onPublic(((IMessageCompound) str).getText());
			} else if(str instanceof Text) {
				onPublic(((Text) str).getText());
			}
		},500);
		super.sendPublic(str);
	}
	/**
	 * @param msg
	 */
	public void onPublic(String msg) {
		logic.onPublic(msg);
	}
	/**
	 * @param msg
	 */
	public void onPrivate(String msg) {
		logic.onPrivate(msg);
	}
	public void sendAsBot(IMessageCompound msg,MsgType type) {
		SBListenerUtils.dispatch(getId().getIdX(), type, msg);
	}
	@Override
	public void sendAsBot(String msg) {
		SBListenerUtils.dispatch(getId().getIdX(),MsgType.PRIVATE,new Text(msg).asMessage());
	}
	@Override
	public void sendAtAsBot(String msg) {
		sendBotMessage("@"+this.getRoom().getHostNameCard()+" "+msg);
		SBListenerUtils.dispatch(getId().getIdX(),MsgType.AT,new Text(msg).asMessage());
	}
	@Override
	public void sendBotMessage(String msg) {
		KooKAdapter.INSTANCE.sendMessage(group,nameCard+"：\n"+msg);
	}
	@Override
	public IMessage getAt() {
		return new Text("@"+nameCard);
	}

	@Override
	public String getMemberString() {
		return nameCard;
	}

	@Override
	public void setNameCard(String s) {
		nameCard=s;
	}

	@Override
	public String getNameCard() {
		return nameCard;
	}

	@Override
	public void tryMute() {
	}

	@Override
	public void tryUnmute() {
	}

	@Override
	public SBId getId() {
		return SBId.of("robot"+String.valueOf(100+rbid));
	}


	@Override
	public void setGame(Game g) {
		sg=g;
		super.group=(Channel) g.getGroup().getInstance();
	}

	@Override
	public void sendPrivate(IMessage str) {
		sg.getScheduler().executeLater(()->{
			if(str instanceof MessageCompound) {
				onPrivate(((IMessageCompound) str).getText());
			} else if(str instanceof Text) {
				onPrivate(((Text) str).getText());
			}
		},500);
	}

	@Override
	public AbstractRoom getRoom() {
		return SBGroup.createInstance(group);
	}

	@Override
	public SBId getHostId() {
		return SBId.of(group.getBot().getId());
	}

	@Override
	public Permission getPermission() {
		return Permission.USER;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + rbid;
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
		SBBotUser other = (SBBotUser) obj;
		if (rbid != other.rbid)
			return false;
		return true;
	}

	@Override
	public void tryAvailable() {
	}



	

}
