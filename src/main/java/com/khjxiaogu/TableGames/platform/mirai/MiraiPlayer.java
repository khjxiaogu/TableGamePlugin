package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.MessageListener;

import net.mamoe.mirai.contact.Group;

public abstract class MiraiPlayer implements AbstractPlayer {
	Group group;

	public MiraiPlayer(Group group) {
		this.group = group;
	}

	@Override
	public void sendPublic(String str) {
		try {
			group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(getAt(),group).plus(str));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(str);
					return;
				}catch(Exception ex2) {}
			}
		}
	}

	@Override
	public void sendPublic(IMessage msg) {
		try {
			group.sendMessage(MiraiAdapter.INSTANCE.toPlatform(getAt(),group).plus(MiraiAdapter.INSTANCE.toPlatform(msg,group)));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					this.sendPrivate(msg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	@Override
	public void registerListener(MessageListener msgc) {
		MiraiListenerUtils.registerListener(getId(),msgc);
	}
	@Override
	public void releaseListener() {
		MiraiListenerUtils.releaseListener(getId());
	}
}
