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
package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.QQId;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.platform.mirai.message.MiraiImage;
import com.khjxiaogu.TableGames.platform.mirai.message.MiraiMessageCompound;
import com.khjxiaogu.TableGames.platform.mirai.message.MiraiPlatformMessage;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.FlashImage;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;

public class MiraiAdapter {
	public final static MiraiAdapter INSTANCE=new MiraiAdapter();
	public IMessage toUnified(net.mamoe.mirai.message.data.Message pmsg,Bot b) {
		return handleMessage(pmsg,b);
	}

	private IMessage handleMessage(net.mamoe.mirai.message.data.Message msg,Bot b) {
		if(msg instanceof MessageChain) {
			MiraiMessageCompound rm=new MiraiMessageCompound((MessageChain) msg,b);
			return rm;
		}else if(msg instanceof PlainText)
			return new Text(((PlainText)msg).getContent());
		else if(msg instanceof net.mamoe.mirai.message.data.At)
			return new At(QQId.of(((net.mamoe.mirai.message.data.At)msg).getTarget()));
		else if(msg instanceof net.mamoe.mirai.message.data.Image)
			return new MiraiImage((net.mamoe.mirai.message.data.Image) msg,b);
		else if (msg instanceof FlashImage) {
			return new MiraiImage((FlashImage)msg,b);
		}
		return new MiraiPlatformMessage(msg);
	}
	private net.mamoe.mirai.message.data.Message handleMessage(IMessage msg,Contact g) {
		if(msg instanceof MessageCompound) {
			MessageChainBuilder rm=new MessageChainBuilder();
			for(IMessage single:(IMessageCompound)msg) {
				rm.append(handleMessage(single,g));
			}
			return rm.asMessageChain();
		}else if(msg instanceof Text)
			return new PlainText(((Text)msg).getText());
		else if(msg instanceof At&&((At) msg).getId()instanceof QQId)
			return new net.mamoe.mirai.message.data.At(((QQId)((At)msg).getId()).getQQId());
		else if(msg instanceof Image)
			return g.uploadImage(ExternalResource.create(((Image) msg).getData()));
		else if(msg instanceof MiraiPlatformMessage)
			return ((MiraiPlatformMessage) msg).getMsg();
		return new PlainText("");
	}
	public net.mamoe.mirai.message.data.Message toPlatform(IMessage umsg,AbstractRoom r) {
		return handleMessage(umsg,(Group) r.getInstance());
	}
	public net.mamoe.mirai.message.data.Message toPlatform(IMessage umsg,Contact r) {
		return handleMessage(umsg,r);
	}


}
