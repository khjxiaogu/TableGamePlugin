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
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;

import love.forte.simbot.definition.GuildMember;
import love.forte.simbot.event.ChannelMessageEvent;


public class SBRoomMessageEvent implements RoomMessageEvent {
	ChannelMessageEvent ev;
	SBHumanUser sender;
	public SBRoomMessageEvent(ChannelMessageEvent event) {
		ev=event;
	}

	@Override
	public AbstractUser getSender() {
		if(sender!=null)return sender;
		return (sender=new SBHumanUser((GuildMember) ev.getAuthor(),ev.getChannel()));
	}

	@Override
	public AbstractRoom getRoom() {
		return SBChannel.createInstance(ev.getChannel());
	}

}
