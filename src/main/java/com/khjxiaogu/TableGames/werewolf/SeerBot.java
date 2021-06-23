package com.khjxiaogu.TableGames.werewolf;

import java.util.ArrayList;
import java.util.List;

public class SeerBot extends GenericBot {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2331883521284417873L;
	List<Villager> checked = new ArrayList<>();
	Villager tocheck;
	boolean toexpose = false;
	boolean exposed = false;

	public SeerBot(int botId, WerewolfGame gam) {
		super(botId, gam);
	}

	@Override
	public void onPrivate(String msg) {
		if (msg.contains("你可以查验一个人")) {
			List<Villager> vx = new ArrayList<>();
			for (Villager v : game.playerlist) {
				if (!v.isDead && !checked.contains(v) && v != roleObject) {
					vx.add(v);
				}
			}
			tocheck = vx.get(rnd.nextInt(vx.size()));
			this.sendAsBot("查验" + tocheck.getId());
			if (!exposed && tocheck instanceof Werewolf) {
				toexpose = true;
			}
			checked.add(tocheck);
			return;
		}
		if (msg.startsWith("请私聊投票")) {
			List<Villager> canvote = game.canVote;
			if (canvote == null) {
				canvote = new ArrayList<>(game.playerlist);
			} else {
				canvote = new ArrayList<>(canvote);
			}
			canvote.removeIf(v -> v.isDead);
			canvote.removeIf(v -> v == roleObject);
			canvote.removeIf(v -> checked.contains(v) && v.getFraction() != Fraction.Wolf);
			if (canvote.size() == 0) {
				this.sendAsBot("弃权");
				return;
			}
			this.sendAsBot("投票" + canvote.get(rnd.nextInt(canvote.size())).getId());
		}
	}

	@Override
	public void onPublic(String msg) {
		if (!exposed && game.getAliveCount() <= 6) {
			toexpose = true;
		}
		if (exposed) {
			sendRole("昨晚", tocheck);
			sendAtAsBot(" 过");
		} else if (toexpose) {
			exposed = true;
			sendBotMessage("这里预言家");
			for (int i = 0; i < checked.size(); i++) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sendRole("第" + i + "晚", checked.get(i));
			}
			sendAtAsBot(" 过");
		} else {
			super.onPublic(msg);
		}
	}

	private void sendRole(String time, Villager tocheck) {
		if (tocheck.isDead) {
			sendBotMessage(time + "查了" + tocheck.getMemberString() + "，他是" + tocheck.getPredictorRole());
		} else {
			sendBotMessage(
					time + "查了" + tocheck.getMemberString() + "，他是" + (tocheck instanceof Werewolf ? "狼人" : "好人"));
		}
	}
}
