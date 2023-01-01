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

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class MiraiUtils {
	public static String getPlainText(MessageChain msg) {
		//return msg.contentToString();
		StringBuilder ptb=new StringBuilder("");
		for(Message m:msg) {
			if(m instanceof PlainText)
				ptb.append(((PlainText) m).getContent());
		}
		return ptb.toString().trim();
	}
	public static At getAt(MessageChain msg) {
		for(Message m:msg) {
			if(m instanceof At)
				return (At) m;
		}
		return null;
	}
	public static Image getImage(MessageChain msg) {
		for(Message m:msg) {
			if(m instanceof Image)
				return (Image) m;
		}
		return null;
	}
}
