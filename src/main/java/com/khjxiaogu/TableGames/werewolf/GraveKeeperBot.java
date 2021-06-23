package com.khjxiaogu.TableGames.werewolf;

public class GraveKeeperBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3372771051275452272L;
	String checked = null;
	boolean exposed = false;

	public GraveKeeperBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPublic(String msg) {
		if ((checked != null || exposed) && msg.contains(GenericBot.talkKey)) {
			game.getScheduler().execute(() -> {
				if (exposed == false) {
					sendBotMessage("守墓人牌");
					exposed = true;
				}
				if (checked != null) {
					sendBotMessage(checked);
				} else {
					sendBotMessage("昨天没有驱逐人。");
					super.sendAtAsBot(" 过");
				}
			});
		}
		super.onPublic(msg);
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("前一天没有驱逐人")) {
			checked = null;
			return;
		} else if (msg.contains("上一个驱逐的是")) {
			checked = msg;
			return;
		}
		super.onPrivate(msg);
	}

}
