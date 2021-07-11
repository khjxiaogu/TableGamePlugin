package com.khjxiaogu.TableGames.platform;

public interface AbstractBotUser extends AbstractUser{

	void sendAsBot(String msg);
	void sendAtAsBot(String msg);
	void sendBotMessage(String msg);
	
}
