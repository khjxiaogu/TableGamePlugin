package com.khjxiaogu.TableGames.fastclue;

import java.util.Random;

import com.khjxiaogu.TableGames.BotPlayer;

public class BotCluePlayer extends BotPlayer {
	FastClueGame game;
	static Random trnd=new Random();
	Random botrandom=new Random(trnd.nextLong());
	public BotCluePlayer(int botId,FastClueGame in) {
		super(botId, in.getGroup(),in);
		game=in;
	}

	@Override
	public void onPrivate(String msg) {
		game.getGroup().getBot().getLogger().info(msg);
		if(msg.startsWith("你可以")) {
			int rgp=botrandom.nextInt(game.players.size());
			int rrl=botrandom.nextInt(game.rooms.size());
			int rwp=botrandom.nextInt(game.weapons.size());
			game.getScheduler().execute(()->super.sendAsBot("假设 "+rgp+""+rrl+""+rwp));
		}else
		if(msg.startsWith("格式"))
			game.getScheduler().execute(()->super.sendAsBot("放弃"));
	}

}
