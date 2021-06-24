package com.khjxiaogu.TableGames.platform.mirai;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.utils.Game;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;


public class MiraiHumanPlayer extends MiraiPlayer implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1931093082691482532L;
	private NormalMember member;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException
	{
		Bot bot=Bot.getInstance(aInputStream.readLong());
		Group g=bot.getGroup(aInputStream.readLong());
		member=g.get(aInputStream.readLong());
	}

	/**
	 * This is the default implementation of writeObject. Customize as necessary.
	 */
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException {
		aOutputStream.writeLong(member.getBot().getId());

		aOutputStream.writeLong(member.getGroup().getId());
		aOutputStream.writeLong(member.getId());
	}
	public MiraiHumanPlayer(NormalMember member) {
		super(member.getGroup());
		this.member = member;
	}
	@Override
	public void sendPrivate(String str) {
		try {
			member.sendMessage(str);
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
	public At getAt() {
		return new At(member.getId());
	}
	@Override
	public String getMemberString() {
		return member.getNameCard()+"("+getId()+")";
	}
	@Override
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	@Override
	public String getNameCard() {
		return member.getNameCard();
	}
	@Override
	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}
	@Override
	public void tryUnmute() {
		try {
			member.unmute();
		} catch (Throwable t) {
		}
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
	public void sendPrivate(IMessage msg) {
		net.mamoe.mirai.message.data.Message pmsg=MiraiAdapter.INSTANCE.toPlatform(msg,group);
		try {
			member.sendMessage(pmsg);
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
}
