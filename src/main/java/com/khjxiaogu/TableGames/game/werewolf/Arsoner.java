package com.khjxiaogu.TableGames.game.werewolf;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class Arsoner extends Villager {
	/**
	 *
	 */
	private static final long serialVersionUID = 1140648613344848439L;
	boolean isSkillUsed = false;

	@Override
	public void onTurn() {
		super.StartTurn();
		if (isSkillUsed)
			return;
		sendPrivate(game.getAliveList());
		super.sendPrivate("纵火者，你可以纵火烧一个人，你有一分钟考虑时间，\n格式：“烧 qq号或者游戏号码”\n如：“烧 1”\n如果放弃纵火，则无需发送任何内容，等待时间结束即可。");
		super.registerListener((msg, type) -> {
			if (type != MsgType.PRIVATE)
				return;
			String content = Utils.getPlainText(msg);
			if (content.startsWith("烧")) {
				try {
					Long qq = Long.parseLong(Utils.removeLeadings("烧", content).replace('号', ' ').trim());
					Villager p = game.getPlayerById(qq);
					if (p == null) {
						super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
						return;
					}
					if (p.isDead()) {
						super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
						return;
					}
					EndTurn();
					super.releaseListener();
					game.logger.logSkill(this, p, "纵火烧");
					isSkillUsed = true;
					p.isBurned = true;
					super.sendPrivate(p.getMemberString() + "被纵火了！");
				} catch (Throwable t) {
					super.sendPrivate("发生错误，正确格式为：“烧 qq号或者游戏号码”！");
				}
			}
		});
	}

	@Override
	public String getJobDescription() {
		return "你有一个一次性纵火技能，可以使一个人的身份公之于众，但是如果此人当晚被狼人刀，则不公开他的身份但是他前边第一个狼人死亡。";
	}

	@Override
	public String getRole() {
		return "纵火者";
	}

	@Override
	public Fraction getRealFraction() {
		return Fraction.God;
	}

	@Override
	public double onVotedAccuracy() {
		if(isSkillUsed)
			return 0.25;
		return 0;
	}

	@Override
	public double onSkilledAccuracy() {
		if(isSkillUsed)
			return 0.25;
		return 0;
	}

	@Override
	public int getTurn() {
		return 2;
	}


	public Arsoner(WerewolfGame game, AbstractUser p) {
		super(game, p);
	}

}
