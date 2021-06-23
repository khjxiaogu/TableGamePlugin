package com.khjxiaogu.TableGames.platform.mirai;

import com.khjxiaogu.TableGames.platform.message.PlatformMessage;

import net.mamoe.mirai.message.data.Message;

public class MiraiPlatformMessage implements PlatformMessage {
	Message msg;

	public MiraiPlatformMessage(Message msg) {
		this.msg = msg;
	}

	public Message getMsg() {
		return msg;
	}

	public void setMsg(Message msg) {
		this.msg = msg;
	}

}
