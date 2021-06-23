package com.khjxiaogu.TableGames.platform.message;

import java.util.ArrayList;
import java.util.Collection;

public class Message extends ArrayList<IMessage> implements IMessage{

	public Message() {
		super();
	}

	public Message(Collection<? extends IMessage> c) {
		super(c);
	}

	public Message(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5093336654457799590L;
	public Message append(String text) {
		this.add(new Text(text));
		return this;
	}
	public Message append(IMessage msg) {
		this.add(msg);
		return this;
	}
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
	@SuppressWarnings("unchecked")
	public <T> T first(Class<T> cls) {
		for(IMessage im:this) {
			if(cls.isInstance(im))
				return (T) im;
		}
		return null;
	}
}
