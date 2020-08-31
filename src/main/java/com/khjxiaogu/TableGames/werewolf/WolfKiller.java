package com.khjxiaogu.TableGames.werewolf;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame.DiedReason;

import net.mamoe.mirai.contact.Member;

public class WolfKiller extends Villager {

	public WolfKiller(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	private Villager lastkill;
	@Override
	public void onTurn() {
		if(game.isFirstNight)return;
		super.StartTurn();
		this.sendPrivate(game.getAliveList());
		super.sendPrivate("猎魔人，你可以选择狩猎一个人。\n如果这个人是狼人，狼人出局。\n如果这个人是好人，你出局。\n格式：“猎杀 qq号或者游戏号码”\n或者可以放弃，格式：“放弃”");
		Utils.registerListener(mid,(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String content=Utils.getPlainText(msg);
			if(content.startsWith("猎杀"))
			try {
				Long qq = Long.parseLong(Utils.removeLeadings("猎杀", content).replace('号', ' ').trim());
				Villager p = game.getPlayerById(qq);
				if (p == null) {
					super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
					return;
				}
				if (p.isDead) {
					super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
					return;
				}
				lastkill=p;
				super.sendPrivate("技能发动成功，请等待第二天早上结果。");
				if(p.getFraction()==Fraction.Wolf) {
					game.kill(p, DiedReason.Hunt);
				}else {
					game.kill(this,DiedReason.Hunt_s);
				}
				Utils.releaseListener(mid);
				super.EndTurn();
			} catch (Throwable t) {
				super.sendPrivate("发生错误，正确格式为：“猎杀 qq号或者游戏号码”！");
			}
			if(content.startsWith("放弃")) {
				Utils.releaseListener(mid);
				super.EndTurn();
			}
		});
	}
	@Override
	public boolean shouldSurvive(DiedReason dir) {
		if(dir==DiedReason.Hunt_s)
			return lastkill.isGuarded;
		if(dir==DiedReason.Poison)
			return true;
		return super.shouldSurvive(dir);
	}
	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}
	@Override
	public int getTurn() {
		return 2;
	}
	@Override
	public String getRole() {
		return "猎魔人";
	}

}
