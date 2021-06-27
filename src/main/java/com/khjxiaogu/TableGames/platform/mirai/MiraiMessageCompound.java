package com.khjxiaogu.TableGames.platform.mirai;

import java.util.AbstractList;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

public class MiraiMessageCompound extends AbstractList<IMessage> implements IMessageCompound  {
	MessageChain mc;
	Bot b;
	public MiraiMessageCompound(MessageChain mc,Bot b) {
		this.mc = mc;
		this.b=b;
	}

	@Override
	public String getText() {
		return Utils.getPlainText(mc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T first(Class<T> cls) {
		if(cls==At.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(Utils.getAt(mc), b);
		}else if(cls==Text.class) {
			for(Message m:mc) {
				if(m instanceof PlainText)
					return (T) MiraiAdapter.INSTANCE.toUnified(m, b);
			}
		}else if(cls==Image.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(Utils.getImage(mc), b);
		}
		return null;
	}

	@Override
	public IMessage get(int index) {
		return MiraiAdapter.INSTANCE.toUnified(mc.get(index),b);
	}

	@Override
	public int size() {
		return mc.size();
	}

}
