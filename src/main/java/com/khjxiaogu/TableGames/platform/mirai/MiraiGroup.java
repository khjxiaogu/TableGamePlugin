package com.khjxiaogu.TableGames.platform.mirai;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.RoomMessageListener;
import com.khjxiaogu.TableGames.platform.message.IMessage;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

public class MiraiGroup implements AbstractRoom,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Map<Long,MiraiGroup> cache=new HashMap<>();
	private long RobotId;
	private long groupId;
	transient private Group group;
	private MiraiGroup(Group group) {
		this.group=group;
	}
	@Override
	public String toString() {
		return "MiraiGroup(" + group.getBot().getId() + "@" + group.getId() + ")";
	}
	public static MiraiGroup createInstance(Group g) {
		MiraiGroup mg=cache.get(g.getId());
		if(mg!=null)return mg;
		mg=new MiraiGroup(g);
		cache.put(g.getId(),mg);
		return mg;
	}
	@Override
	public int hashCode() {
		return group.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MiraiGroup other = (MiraiGroup) obj;
		if (group == null) {
			if (other.group != null)
				return false;
		} else if (!group.equals(other.group))
			return false;
		return true;
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
	public AbstractUser getOwner() {
		return new MiraiHumanUser(group.getOwner());
	}

	@Override
	public AbstractUser get(long id) {
		MiraiHumanUser m;
		m=new MiraiHumanUser(group.get(id));
		return m;
	}

	@Override
	public void sendMessage(IMessage msg) {
		SlowUtils.runSlowly(()->group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(msg,this)));
	}

	@Override
	public void sendMessage(String msg) {
		SlowUtils.runSlowly(()->group.sendMessage(msg));
	}

	@Override
	public Object getInstance() {
		return group;
	}
	@Override
	public void registerRoomListener(RoomMessageListener ml) {
		MiraiListenerUtils.registerListener(group, ml);
	}
	@Override
	public void releaseRoomListener() {
		
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
	@Override
	public long getId() {
		return group.getId();
	}
}
