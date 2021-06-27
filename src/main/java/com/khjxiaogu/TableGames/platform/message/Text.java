package com.khjxiaogu.TableGames.platform.message;

public class Text implements IMessage {
	String text;

	public Text(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	public MessageCompound asMessage() {
		return new MessageCompound().append(this);
	}
}
