/**
 * Mirai Song Plugin
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
package com.khjxiaogu.TableGames.platform.message;

import java.util.ArrayList;
import java.util.Collection;

public class MessageCompound extends ArrayList<IMessage> implements IMessageCompound{

	public MessageCompound() {
		super();
	}

	public MessageCompound(Collection<? extends IMessage> c) {
		super(c);
	}

	public MessageCompound(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5093336654457799590L;
	public MessageCompound append(String text) {
		this.add(new Text(text));
		return this;
	}
	public MessageCompound append(IMessage msg) {
		this.add(msg);
		return this;
	}
	@Override
	public String getText() {
		StringBuilder sb=new StringBuilder();
		for(IMessage im:this) {
			if(im instanceof Text) {
				sb.append(im);
			} else {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	@Override
	@SuppressWarnings("unchecked")
	public <T> T first(Class<T> cls) {
		for(IMessage im:this) {
			if(cls.isInstance(im))
				return (T) im;
		}
		return null;
	}
}
