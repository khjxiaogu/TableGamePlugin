package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener;
import com.khjxiaogu.TableGames.platform.Permission;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import net.mamoe.mirai.contact.User;

public class MiraiUserFriend implements AbstractUser {
	User member;
	public MiraiUserFriend(User sender) {
		this.member=sender;
	}

	@Override
	public void sendPrivate(String str) {
		try {
			SlowUtils.runSlowly(()->member.sendMessage(str));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					member.sendMessage(str);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	@Override
	public int hashCode() {
		return (int) getId();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractUser))
			return false;
		AbstractUser other = (AbstractUser) obj;
		return getId()==other.getId();
	}

	@Override
	public void sendPrivate(IMessage msg) {
		net.mamoe.mirai.message.data.Message pmsg=MiraiAdapter.INSTANCE.toPlatform(msg,member);
		try {
			SlowUtils.runSlowly(()->member.sendMessage(pmsg));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					member.sendMessage(pmsg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	@Override
	public void sendPublic(String str) {
	}

	@Override
	public void sendPublic(IMessage msg) {
	}

	@Override
	public IMessage getAt() {
		return null;
	}

	@Override
	public String getMemberString() {
		return member.getNick();
	}

	@Override
	public void setNameCard(String s) {
	}

	@Override
	public String getNameCard() {
		return null;
	}

	@Override
	public void tryMute() {
	}

	@Override
	public void tryUnmute() {
	}

	@Override
	public long getId() {
		return member.getId();
	}

	@Override
	public void bind(Object obj) {
	}

	@Override
	public void setGame(Game g) {
	}

	@Override
	public void registerListener(MessageListener msgc) {
	}

	@Override
	public void releaseListener() {
	}

	@Override
	public Object getRoleObject() {
		return null;
	}
	@Override
	public AbstractRoom getRoom() {
		return null;
	}

	@Override
	public long getHostId() {
		return member.getBot().getId();
	}
	@Override
	public Permission getPermission() {
		return Permission.USER;
	}
}
