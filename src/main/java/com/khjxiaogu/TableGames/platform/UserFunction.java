package com.khjxiaogu.TableGames.platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.utils.Game;

public class UserFunction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8489601792669124710L;
	protected AbstractUser member;
	public AbstractUser getMember() {
		return member;
	}
	public void setMember(AbstractUser member) {
		this.member = member;
	}
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		aInputStream.defaultReadObject();
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.defaultWriteObject();
	}
	public UserFunction(AbstractUser p) {
		member = p;
	}
	public void sendPrivate(String str) {
		member.sendPrivate(str);
	}
	public void sendPublic(String str) {
		member.sendPublic(str);
	}
	public void sendPublic(MessageCompound msg) {
		member.sendPublic(msg);
	}
	public IMessage getAt() {
		return member.getAt();
	}
	public String getMemberString() {
		return member.getMemberString();
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	public void registerListener(MessageListener msgc) {
		member.registerListener(msgc);
	}
	public void releaseListener() {
		member.releaseListener();
	}
	public String getNameCard() {
		return member.getNameCard();
	}
	public void tryMute() {
		member.tryMute();
	}
	public void tryUnmute() {
		member.tryUnmute();
	}
	public long getId() {
		return member.getId();
	}
	public final void bind(Object obj) {
		member.bind(obj);
	}
	public void setGame(Game g) {
		member.setGame(g);
	}
	public Object getRoleObject() {
		return member.getRoleObject();
	}
}

