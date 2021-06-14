package com.khjxiaogu.TableGames;

import java.io.Serializable;

import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

public class BotPlayer implements AbstractPlayer,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3890210129608510286L;
	protected int rbid;
	protected String nameCard;
	transient protected Group gp;
	transient Game sg;
	transient protected Object roleObject;
	public BotPlayer(int botId,Group in,Game g) {
		rbid=botId;
		nameCard="机器人"+rbid;
		gp=in;
		sg=g;
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
		gp.sendMessage(getAt().plus(str));
	}

	@Override
	public void sendPublic(Message str) {
		sg.getScheduler().executeLater(()->{
		if(str instanceof MessageChain)
			onPublic(Utils.getPlainText((MessageChain) str));
		else
			onPublic(str.contentToString());
		},500);
		gp.sendMessage(getAt().plus(str));
	}
	/**
	 * @param msg  
	 */
	public void onPublic(String msg) {
		
	}
	/**
	 * @param msg  
	 */
	public void onPrivate(String msg) {
		
	}
	public void sendAsBot(MessageChain msg,MsgType type) {
		ListenerUtils.dispatch(getId(), type, msg);
	}
	public void sendAsBot(String msg) {
		ListenerUtils.dispatch(getId(),MsgType.PRIVATE,new MessageChainBuilder().append(msg).asMessageChain());
	}
	public void sendAtAsBot(String msg) {
		sendBotMessage(msg);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ListenerUtils.dispatch(getId(),MsgType.AT,new MessageChainBuilder().append(msg).asMessageChain());
	}
	public void sendBotMessage(String msg) {
		gp.sendMessage(this.nameCard+"：\n"+msg);
	}
	@Override
	public Message getAt() {
		return new PlainText("@"+nameCard);
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
	public void bind(Object obj) {
		roleObject=obj;
	}

	@Override
	public void setGame(Game g) {
		sg=g;
		gp=g.getGroup();
	}

}
