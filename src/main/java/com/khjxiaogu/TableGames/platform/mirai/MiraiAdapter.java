package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.MessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
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
			return new At(((net.mamoe.mirai.message.data.At)msg).getTarget());
		else if(msg instanceof net.mamoe.mirai.message.data.Image)
			return new MiraiImage((net.mamoe.mirai.message.data.Image) msg,b);
		return new MiraiPlatformMessage(msg);
	}
	private net.mamoe.mirai.message.data.Message handleMessage(IMessage msg,Group g) {
		if(msg instanceof MessageCompound) {
			MessageChainBuilder rm=new MessageChainBuilder();
			for(IMessage single:(IMessageCompound)msg) {
				rm.append(handleMessage(single,g));
			}
			return rm.asMessageChain();
		}else if(msg instanceof Text)
			return new PlainText(((Text)msg).getText());
		else if(msg instanceof At)
			return new net.mamoe.mirai.message.data.At(((At)msg).getId());
		else if(msg instanceof Image)
			return g.uploadImage(ExternalResource.create(((Image) msg).getData()));
		else if(msg instanceof MiraiPlatformMessage)
			return ((MiraiPlatformMessage) msg).getMsg();
		return new PlainText("");
	}
	public net.mamoe.mirai.message.data.Message toPlatform(IMessage umsg,AbstractRoom r) {
		return handleMessage(umsg,(Group) r.getInstance());
	}
	public net.mamoe.mirai.message.data.Message toPlatform(IMessage umsg,Group r) {
		return handleMessage(umsg,r);
	}
}
