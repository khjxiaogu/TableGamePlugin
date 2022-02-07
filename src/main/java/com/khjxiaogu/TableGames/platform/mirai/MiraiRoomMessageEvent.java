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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;

import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.events.GroupMessageEvent;

public class MiraiRoomMessageEvent implements RoomMessageEvent {
	GroupMessageEvent ev;
	MiraiHumanUser sender;
	public MiraiRoomMessageEvent(GroupMessageEvent event) {
		ev=event;
	}

	@Override
	public AbstractUser getSender() {
		if(sender!=null)return sender;
		return (sender=new MiraiHumanUser((NormalMember) ev.getSender()));
	}

	@Override
	public AbstractRoom getRoom() {
		return MiraiGroup.createInstance(ev.getGroup());
	}

}
