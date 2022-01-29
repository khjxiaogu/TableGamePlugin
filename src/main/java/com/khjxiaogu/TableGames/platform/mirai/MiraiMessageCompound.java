package com.khjxiaogu.TableGames.platform.mirai;

import java.util.AbstractList;

import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.IMessage;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.Text;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;

public class MiraiMessageCompound extends AbstractList<IMessage> implements IMessageCompound  {
	MessageChain mc;
	Bot b;
	public MiraiMessageCompound(MessageChain mc,Bot b) {
		this.mc = mc;
		this.b=b;
	}

	@Override
	public String getText() {
		return MiraiUtils.getPlainText(mc);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T first(Class<T> cls) {
		if(cls==At.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(MiraiUtils.getAt(mc), b);
		}else if(cls==Text.class) {
			return (T) new Text(MiraiUtils.getPlainText(mc));
		}else if(cls==Image.class) {
			return (T) MiraiAdapter.INSTANCE.toUnified(MiraiUtils.getImage(mc), b);
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
