package com.khjxiaogu.TableGames;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

public class HumanPlayer implements AbstractPlayer,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1931093082691482532L;
	private Member member;
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException 
    {
    	Bot bot=MiraiConsole.INSTANCE.getBotOrNull(aInputStream.readLong());
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
	public HumanPlayer(Member member) {
		this.member = member;
	}
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
	public void sendPublic(String str) {
		try {
			member.getGroup().sendMessage(this.getAt().plus(str));
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
	public void sendPublic(Message msg) {
		try {
			member.getGroup().sendMessage(this.getAt().plus(msg));
		}catch(Exception ex) {
			while(true) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {}
				try {
					member.sendMessage(msg);
					return;
				}catch(Exception ex2) {}
			}
		}
	}
	public Message getAt() {
		return new At(member);
	}
	public String getMemberString() {
		return member.getNameCard()+"("+getId()+")";
	}
	public void setNameCard(String s) {
		member.setNameCard(s);
	}
	public String getNameCard() {
		return member.getNameCard();
	}
	public void tryMute() {
		try {
			member.mute(3600);
		} catch (Throwable t) {
		}
	}
	public void tryUnmute() {
		try {
			member.unmute();
		} catch (Throwable t) {
		}
	}
	public long getId() {
		return member.getId();
	}
	@Override
	public void bind(Object obj) {
	}

	@Override
	public void setGame(Game g) {
	}
}
