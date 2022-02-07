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

import java.util.AbstractList;

import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.Text;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;

public class MiraiMessageCompound extends AbstractList<IMessage> implements IMessageCompound  {
	MessageChain mc;
	Bot b;
	public MiraiMessageCompound(MessageChain mc,Bot b) {
		this.mc = mc;
		this.b=b;
	}

	@Override
	public String getText() {
		return MiraiUtils.getPlainText(mc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T first(Class<T> cls) {
		if(cls==At.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(MiraiUtils.getAt(mc), b);
		}else if(cls==Text.class) {
			return (T) new Text(MiraiUtils.getPlainText(mc));
		}else if(cls==Image.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(MiraiUtils.getImage(mc), b);
		}
		return null;
	}

	@Override
	public IMessage get(int index) {
		return MiraiAdapter.INSTANCE.toUnified(mc.get(index),b);
	}

	@Override
	public int size() {
		return mc.size();
	}

}
