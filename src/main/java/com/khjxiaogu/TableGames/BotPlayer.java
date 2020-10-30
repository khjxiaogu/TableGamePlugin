package com.khjxiaogu.TableGames;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;

public class BotPlayer implements AbstractPlayer {
	int rbid;
	String nameCard;
	Group gp;
	public BotPlayer(int botId,Group in) {
		rbid=botId;
		nameCard="机器人"+rbid;
		gp=in;
	}

	@Override
	public void sendPrivate(String str) {
		onPrivate(str);
	}

	@Override
	public void sendPublic(String str) {
		onPublic(str);
		gp.sendMessage(getAt().plus(str));
	}

	@Override
	public void sendPublic(Message str) {
		if(str instanceof MessageChain)
			onPublic(Utils.getPlainText((MessageChain) str));
		else
			onPublic(str.contentToString());
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

}
