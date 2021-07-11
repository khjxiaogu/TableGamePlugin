package com.khjxiaogu.TableGames.platform;

import java.io.Serializable;

public interface BotUserLogic extends Serializable {
	void onPublic(String msg);
	void onPrivate(String msg);
}
