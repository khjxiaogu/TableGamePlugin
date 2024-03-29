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
import java.security.SecureRandom;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.QQId;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.IMessage;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.PlainText;

public abstract class MiraiUser implements AbstractUser,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5656814787243115641L;

	transient Group group;
	transient protected Object roleObject;
	
	@Override
	public void bind(Object obj) {
		roleObject=obj;
	}

	public MiraiUser(Group group) {
		this.group = group;
	}

	@Override
	public void sendPublic(String str) {
		try {
			
			SlowUtils.runSlowly(()->group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(getAt(),group).plus(str)));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(str);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	@Override
	public void sendPublic(IMessage msg) {
		try {
			SlowUtils.runSlowly(()->group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(getAt(),group).plus(MiraiAdapter.INSTANCE.toPlatform(msg,group))));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(msg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	@Override
	public abstract QQId getId();
	@Override
	public Object getRoleObject() {
		return roleObject;
	}
	@Override
	public UserIdentifier getHostId() {
		return QQId.of(group.getBot().getId());
	}
	@Override
	public AbstractRoom getRoom() {
		return MiraiGroup.createInstance(group);
	}

	@Override
	public void sendForName(String str) {
		try {
			
			SlowUtils.runSlowly(()->group.sendMessage(this.getMemberString()+" "+str));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(str);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	@Override
	public void sendForName(IMessage msg) {
		try {
			SlowUtils.runSlowly(()->group.sendMessage(new PlainText(this.getMemberString()).plus(MiraiAdapter.INSTANCE.toPlatform(msg,group))));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(msg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	@Override
	public void tryAvailable() {
		try {
			if(group.getBot().getFriend(this.getId().getQQId())!=null)return;
		}catch(Exception ex) {
			
		}
		this.sendPublic(" 为了游戏顺利进行，请加我。");
	}
}
