package com.khjxiaogu.TableGames.platform.mirai;

import java.io.Serializable;
import com.khjxiaogu.TableGames.platform.AbstractBotUser;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.BotUserLogic;
import com.khjxiaogu.TableGames.platform.MessageListener.MsgType;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Group;


public class MiraiBotUser extends MiraiUser implements Serializable,AbstractBotUser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3890210129608510286L;
	protected int rbid;
	transient Game sg;
	protected String nameCard;
	private BotUserLogic logic;
	
	public MiraiBotUser(int botId,Group in,Game g) {
		super(in);
		rbid=botId;
		nameCard="机器人"+rbid;
		sg=g;
	}

	public MiraiBotUser(int botId,AbstractRoom group,Class<? extends BotUserLogic> logicType, Game in) {
		this(botId,(Group) group.getInstance(),in);
		logic=Utils.createLogic(logicType,this,in);
	}

	@Override
	public void sendPrivate(String str) {
		onPrivate(str);
	}

	@Override
	public void sendPublic(String str) {
		sg.getScheduler().executeLater(()->{
			onPublic(str);
		},500);
		super.sendPublic(str);
	}

	@Override
	public void sendPublic(IMessage str) {
		sg.getScheduler().executeLater(()->{
			if(str instanceof MessageCompound) {
				onPublic(((IMessageCompound) str).getText());
			} else if(str instanceof Text) {
				onPublic(((Text) str).getText());
			}
		},500);
		super.sendPublic(str);
	}
	/**
	 * @param msg
	 */
	public void onPublic(String msg) {
		logic.onPublic(msg);
	}
	/**
	 * @param msg
	 */
	public void onPrivate(String msg) {
		logic.onPrivate(msg);
	}
	public void sendAsBot(IMessageCompound msg,MsgType type) {
		MiraiListenerUtils.dispatch(getId(), type, msg);
	}
	@Override
	public void sendAsBot(String msg) {
		MiraiListenerUtils.dispatch(getId(),MsgType.PRIVATE,new Text(msg).asMessage());
	}
	@Override
	public void sendAtAsBot(String msg) {
		sendBotMessage(msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MiraiListenerUtils.dispatch(getId(),MsgType.AT,new Text(msg).asMessage());
	}
	@Override
	public void sendBotMessage(String msg) {
		super.group.sendMessage(nameCard+"：\n"+msg);
	}
	@Override
	public IMessage getAt() {
		return new Text("@"+nameCard);
	}

	@Override
	public String getMemberString() {
		return nameCard;
	}

	@Override
	public void setNameCard(String s) {
		nameCard=s;
	}

	@Override
	public String getNameCard() {
		return nameCard;
	}

	@Override
	public void tryMute() {
	}

	@Override
	public void tryUnmute() {
	}

	@Override
	public long getId() {
		return 100+rbid;
	}


	@Override
	public void setGame(Game g) {
		sg=g;
		super.group=(Group) g.getGroup().getInstance();
	}

	@Override
	public void sendPrivate(IMessage str) {
		sg.getScheduler().executeLater(()->{
			if(str instanceof MessageCompound) {
				onPrivate(((IMessageCompound) str).getText());
			} else if(str instanceof Text) {
				onPrivate(((Text) str).getText());
			}
		},500);
	}

	

}
