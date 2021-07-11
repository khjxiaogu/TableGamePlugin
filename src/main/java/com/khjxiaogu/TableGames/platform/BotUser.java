package com.khjxiaogu.TableGames.platform;

public class BotUser implements BotUserLogic {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6179212605268629887L;
	private AbstractBotUser internal;
	public BotUser(AbstractBotUser internal) {
		this.internal = internal;
	}

	@Override
	public void onPublic(String msg) {
	}

	@Override
	public void onPrivate(String msg) {
	}

	public AbstractBotUser getPlayer() {
		return internal;
	}

}
