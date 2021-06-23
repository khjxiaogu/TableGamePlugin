package com.khjxiaogu.TableGames.werewolf;

import java.util.ArrayList;
import java.util.List;

public class WitchBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = 741149304485873268L;
	boolean selfrescue = false;
	boolean usedrescue = false;
	boolean exposed = false;
	int lastrescue = -1;
	int lastpoisoned = -1;

	public WitchBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("今晚死亡情况是：") && !msg.contains("今晚没有人死亡")) {
			if (game.tokill.contains(roleObject)) {
				if (game.isFirstNight) {
					save(game.playerlist.indexOf(roleObject));
					selfrescue = true;
				} else {
					decidePoison();
				}
			} else {
				save(game.playerlist.indexOf(game.tokill.iterator().next()));
			}
			usedrescue = true;
			return;
		}
		if (msg.contains("女巫，你有")) {
			if (exposed) {
				decidePoison();
			}
			return;
		}
		super.onPrivate(msg);
	}

	@Override
	public void onPublic(String msg) {
		if (!exposed && game.getAliveCount() <= 6) {
			exposed = true;
		}
		if (msg.contains(GenericBot.talkKey)) {
			if (usedrescue) {
				usedrescue = false;
				exposed = true;
				if (!selfrescue) {
					sendBotMessage("女巫，" + lastrescue + "号银水，今晚随机毒一个。");
				} else {
					sendBotMessage("女巫，昨晚自救，今晚随机毒一个。");
				}
				sendAtAsBot(" 过");
				return;
			}
			if (exposed) {
				if (lastpoisoned != -1) {
					sendBotMessage("女巫，昨晚毒了" + lastpoisoned + "号");
				} else {
					sendBotMessage("女巫牌");
				}
				sendAtAsBot(" 过");
				return;
			}
		}
		super.onPublic(msg);
	}

	private void save(int num) {
		lastrescue = num;
		this.sendAsBot("救" + num);
	}

	private void decidePoison() {
		List<Villager> vx = new ArrayList<>();
		for (Villager v : game.playerlist) {
			if (v.getFraction() != Fraction.God) {
				vx.add(v);
			}
		}
		int l = game.playerlist.indexOf(vx.get(rnd.nextInt(vx.size())));
		lastpoisoned = l;
		this.sendAsBot("毒" + l);
	}
}
