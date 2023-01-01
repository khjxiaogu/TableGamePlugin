/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.platform.simplerobot;

import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.platform.simplerobot.message.SBImage;
import com.khjxiaogu.TableGames.platform.simplerobot.message.SBMessageCompound;
import com.khjxiaogu.TableGames.platform.simplerobot.message.SBPlatformMessage;

import love.forte.simbot.action.SendSupport;
import love.forte.simbot.bot.Bot;
import love.forte.simbot.component.kook.message.KookKMarkdownMessage;
import love.forte.simbot.kook.objects.AtTarget;
import love.forte.simbot.kook.objects.KMarkdownBuilder;
import love.forte.simbot.message.Message.Element;
import love.forte.simbot.message.Messages;
import love.forte.simbot.message.PlainText;
import love.forte.simbot.resources.Resource;



public class KooKAdapter {
	public final static KooKAdapter INSTANCE=new KooKAdapter();
	public IMessage toUnified(love.forte.simbot.message.Message pmsg,Bot b) {
		return handleMessage(pmsg,b);
	}

	private IMessage handleMessage(love.forte.simbot.message.Message msg,Bot b) {
		if(msg instanceof Messages) {
			SBMessageCompound rm=new SBMessageCompound((Messages) msg,b);
			return rm;
		}else if(msg instanceof PlainText)
			return new Text(((PlainText)msg).getText());
		else if(msg instanceof love.forte.simbot.message.At)
			return new At(SBId.of(((love.forte.simbot.message.At)msg).getTarget()));
		else if(msg instanceof love.forte.simbot.message.Image)
			return new SBImage((love.forte.simbot.message.Image) msg,b);
		return new SBPlatformMessage(msg);
	}
	private Messages unwarp(Messages old,love.forte.simbot.message.Message msg){
		if(msg instanceof Element)
			return old.plus((Element<?>) msg);
		for(love.forte.simbot.message.Message single:(Messages)msg) {
			old=unwarp(old,single);
		}
		return old;
	}
	interface SendWrapper{
		void sendText(String msg);
		void sendMarkdown(String msg);
		void sendImage(String msg);
	}

	public void sendMessage(SendSupport ss,String msgx) {
		GlobalMain.getLogger().info(msgx);
		ss.sendBlocking(msgx);
		//ss.sendAsync(KookKMarkdownMessage.asMessage(new KMarkdownBuilder().text(msgx).build()));
	}
	public void sendMessage(SendSupport ss,IMessage msgx,Bot g) {
		GlobalMain.getLogger().info(msgx.asMessage().getText());
		if(msgx instanceof IMessageCompound) {
			((IMessageCompound)msgx).flatern();
			KMarkdownBuilder kmdb=new KMarkdownBuilder();
			boolean has=false;
			for(IMessage single:(IMessageCompound)msgx) {
				if(single instanceof Text) {
					has=true;
					kmdb.text(((Text)single).getText());
				}else if(single instanceof At) {
					has=true;
					kmdb.at(new AtTarget.User(((SBId)((At)single).getId()).getIdX().toString()));
				}else if(single instanceof Image) {
					ss.sendBlocking(new KookKMarkdownMessage(kmdb.build()));
					kmdb=new KMarkdownBuilder();
					has=false;
					ss.sendBlocking(love.forte.simbot.message.Image.of(Resource.of(((Image) single).getData(),"MessageImage.jpg")));
				}else if(single instanceof SBPlatformMessage) {
					ss.sendBlocking(new KookKMarkdownMessage(kmdb.build()));
					kmdb=new KMarkdownBuilder();
					has=false;
					if(ss instanceof SendSupport)
					((SendSupport) ss).sendBlocking(((SBPlatformMessage) single).getMsg());
				}
			}
			if(has)
				ss.sendBlocking(new KookKMarkdownMessage(kmdb.build()));
			
		}else if(msgx instanceof Text)
			ss.sendBlocking(((Text)msgx).getText());
		else if(msgx instanceof At)
			ss.sendBlocking(new KookKMarkdownMessage(new KMarkdownBuilder().at(new AtTarget.User(((SBId)((At)msgx).getId()).getIdX().toString())).build()));
		else if(msgx instanceof Image)
			ss.sendBlocking(love.forte.simbot.message.Image.of(Resource.of(((Image) msgx).getData(),"MessageImage.jpg")));
		else if(msgx instanceof SBPlatformMessage)
			if(ss instanceof SendSupport)
			((SendSupport) ss).sendBlocking(((SBPlatformMessage) msgx).getMsg());
		
	}
	private love.forte.simbot.message.Message handleMessage(IMessage msg,Bot g) {
		if(msg instanceof MessageCompound) {
			Messages msgs=Messages.messages();
			for(IMessage single:(IMessageCompound)msg) {
				msgs=unwarp(msgs,handleMessage(single,g));
			}
			return msgs;
		}else if(msg instanceof Text)
			return love.forte.simbot.message.Text.of(((Text)msg).getText());
		else if(msg instanceof At)
			return KookKMarkdownMessage.asMessage(new KMarkdownBuilder().at(new AtTarget.User(((SBId)((At)msg).getId()).getIdX().toString())).build());
		else if(msg instanceof Image)
			return love.forte.simbot.message.Image.of(Resource.of(((Image) msg).getData(),"MessageImage.jpg"));
		else if(msg instanceof SBPlatformMessage)
			return ((SBPlatformMessage) msg).getMsg();
		return love.forte.simbot.message.Text.getEmptyText();
	}
	public love.forte.simbot.message.Message toPlatform(IMessage umsg,Bot b) {
		return handleMessage(umsg,b);
		
	}

}
