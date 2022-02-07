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

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;


public class MiraiHumanUser extends MiraiUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1931093082691482532L;
	private transient NormalMember member;
	private long bid;
	private long gid;
	private long mid;
	private QQId id;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		aInputStream.defaultReadObject();
		Bot bot=Bot.getInstance(bid);
		group=bot.getGroup(gid);
		member=group.get(mid);
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		bid=member.getBot().getId();

		gid=member.getGroup().getId();
		mid=member.getId();
		aOutputStream.defaultWriteObject();
	}
	@Override
	public int hashCode() {
		return Long.hashCode(member.getId());
	}

	public MiraiHumanUser(NormalMember member) {
		super(member.getGroup());
		this.member = member;
		this.id=QQId.of(member.getId());
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
					SlowUtils.runSlowly(()->member.sendMessage(str));
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	@Override
	public At getAt() {
		return new At(id);
	}
	@Override
	public String getMemberString() {
		return member.getNameCard()/*+"("+getId()+")"*/;
	}
	@Override
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	@Override
	public String getNameCard() {
		String nc=member.getNameCard();
		if(nc==null||nc.length()==0)
		return member.getNick();
		return nc;
	}
	@Override
	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}
	@Override
	public void tryUnmute() {
		try {
			member.unmute();
		} catch (Throwable t) {
		}
	}
	@Override
	public QQId getId() {
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
		net.mamoe.mirai.message.data.Message pmsg=MiraiAdapter.INSTANCE.toPlatform(msg,group);
		try {
			SlowUtils.runSlowly(()->member.sendMessage(pmsg));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					SlowUtils.runSlowly(()->member.sendMessage(pmsg));
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	@Override
	public Permission getPermission() {
		MemberPermission mp=member.getPermission();
		switch(mp) {
		case OWNER:return Permission.SYSTEM;
		case ADMINISTRATOR:return Permission.ADMIN;
		default:return Permission.USER;
		}
	}
}
