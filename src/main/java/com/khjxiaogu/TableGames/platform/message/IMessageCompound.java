package com.khjxiaogu.TableGames.platform.message;

import java.util.List;

public interface IMessageCompound extends List<IMessage>,IMessage {

	String getText();

	<T> T first(Class<T> cls);

}