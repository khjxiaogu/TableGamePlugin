package com.khjxiaogu.TableGames.platform.mirai;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.MessageListener;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

public class MiraiGroup implements AbstractRoom,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private long RobotId;
	private long groupId;
	transient private Group group;
	public MiraiGroup(Group group) {
		this.group=group;
	}

	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		// perform the default de-serialization first
		aInputStream.defaultReadObject();

		group=Bot.getInstance(RobotId).getGroup(groupId);
		// make defensive copy of the mutable Date field
		// ensure that object state has not been corrupted or tampered with malicious code
		//validateUserInfo();
	}

	/**
	 * This is the default implementation of writeObject. Customize as necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {

		//ensure that object is in desired state. Possibly run any business rules if applicable.
		//checkUserInfo();
		groupId=group.getId();
		RobotId=group.getBot().getId();
		// perform the default serialization for all non-transient, non-static fields
		aOutputStream.defaultWriteObject();

	}

	@Override
	public AbstractPlayer getOwner() {
		return new MiraiHumanPlayer(group.getOwner());
	}

	@Override
	public AbstractPlayer get(long id) {
		return new MiraiHumanPlayer(group.get(id));
	}

	@Override
	public void sendMessage(IMessage msg) {
		group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(msg,this));
	}

	@Override
	public void sendMessage(String msg) {
		group.sendMessage(msg);
	}

	@Override
	public Object getInstance() {
		return group;
	}

	@Override
	public void registerListener(Long id, MessageListener ml) {
		MiraiListenerUtils.registerListener(id, group, ml);
	}

	@Override
	public void releaseListener(long id) {
		MiraiListenerUtils.releaseListener(id);
	}

	@Override
	public void setMuteAll(boolean isMute) {
		group.getSettings().setMuteAll(isMute);
	}

	@Override
	public String getHostNameCard() {
		return group.getBotAsMember().getNameCard();
	}
}
