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

import java.io.Serializable;

import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.BotUserLogic;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.QQId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Group;


public class MiraiBotUser extends MiraiUser implements Serializable,AbstractBotUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3890210129608510286L;
	protected int rbid;
	transient Game sg;
	protected String nameCard;
	private BotUserLogic logic;
	
	public MiraiBotUser(int rbid,Group in) {
		super(in);
		nameCard="机器人"+(100+rbid);
		this.rbid=rbid;
	}

	public MiraiBotUser(int rbid,AbstractRoom group) {
		this(rbid,(Group) group.getInstance());
		
	}
	public void setLogic(Class<? extends BotUserLogic> logicType) {
		if(logicType!=null)
			logic=Utils.createLogic(logicType,this,sg);
		else {
			sg=null;
			logic=null;
		}
	}
	@Override
	public void sendPrivate(String str) {
		onPrivate(str);
	}

	@Override
	public void sendPublic(String str) {
		onPublic(str);
		super.sendPublic(str);
	}

	@Override
	public void sendPublic(IMessage str) {
		if(str instanceof MessageCompound) {
			onPublic(((IMessageCompound) str).getText());
		} else if(str instanceof Text) {
			onPublic(((Text) str).getText());
		}
		super.sendPublic(str);
	}
	/**
	 * @param msg
	 */
	public void onPublic(String msg) {
		if(logic!=null)
			logic.onPublic(msg);
	}
	/**
	 * @param msg
	 */
	public void onPrivate(String msg) {
		if(logic!=null)
			logic.onPrivate(msg);
	}
	@Override
	public void sendBotMessage(String msg) {
		SlowUtils.runSlowly(()->super.group.sendMessage(nameCard+"：\n"+msg));
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
	public QQId getId() {
		return QQId.of(100+rbid);
	}


	@Override
	public void setGame(Game g) {
		sg=g;
		super.group=(Group) g.getGroup().getInstance();
	}

	@Override
	public void sendPrivate(IMessage str) {
		if(sg!=null)
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
		return MiraiGroup.createInstance(group);
	}

	@Override
	public QQId getHostId() {
		return QQId.of(group.getBot().getId());
	}

	@Override
	public Permission getPermission() {
		return Permission.USER;
	}
	@Override
	public void requestOperation(boolean isPublic,boolean isTemperal,String temperalHint) {
		logic.requestOperation(isPublic,isTemperal,temperalHint);
	}
	public void receiveMessage(UserIdentifier id,String msg,boolean isPublic) {
		logic.receivedGameMessage(id, msg, isPublic);
	};
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
		MiraiBotUser other = (MiraiBotUser) obj;
		if (rbid != other.rbid)
			return false;
		return true;
	}

	@Override
	public void tryAvailable() {
	}

	@Override
	public boolean isFriend() {
		return true;
	}

	@Override
	public boolean hasLogic() {
		return logic!=null;
	}
	int refcount;
	@Override
	public void addRef() {
		refcount++;
	}

	@Override
	public void release() {
		refcount--;
	}

	@Override
	public boolean isReferred() {
		return refcount>0;
	}



	

}
