package com.khjxiaogu.TableGames.depravekill;

import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.depravekill.WerewolfGame.DiedReason;
import com.khjxiaogu.TableGames.depravekill.WerewolfGame.WaitReason;

import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.PlainText;

public class Hunter extends Villager {
	boolean hasGun=true;
	boolean asked=false;
	@Override
	public boolean onDiePending(DiedReason dir) {
		super.StartTurn();
		dr=dir;
		if(!hasGun||asked)return false;
		if(dir.canUseSkill) {
			this.sendPrivate(game.getAliveList());
			super.sendPrivate("猎人，你死了，你可以选择翻牌并开枪打死另一个人，你有30秒的考虑时间\n格式：“杀死 qq号或者游戏号码”\n如：“杀死 1”\n如果不需要，则等待时间结束即可。");
			asked=true;
			ListenerUtils.registerListener(super.getId(),(msg,type)->{
				if((dir==DiedReason.Vote||dir==DiedReason.Explode||game.isFirstNight)&&type==MsgType.AT) {
					ListenerUtils.releaseListener(getId());
					game.skipWait(WaitReason.DieWord);
				}
				if(type!=MsgType.PRIVATE)return;
				String content=Utils.getPlainText(msg);
				if(content.startsWith("杀死")) {
					try {
						Long qq=Long.parseLong(Utils.removeLeadings("杀死",content).replace('号', ' ').trim());
						Villager p=game.getPlayerById(qq);
						if(p==null) {
							super.sendPrivate("选择的qq号或者游戏号码非游戏玩家，请重新输入");
							return;
						}
						if(p.isDead) {
							super.sendPrivate("选择的qq号或者游戏号码已死亡，请重新输入");
							return;
						}
						if(p.equals(this)) {
							super.sendPrivate("选择的qq号或者游戏号码是你自己，请重新输入");
							return;
						}
						this.EndTurn();
						ListenerUtils.releaseListener(super.getId());
						if(dir==DiedReason.Vote||dir==DiedReason.Explode)
							ListenerUtils.registerListener(super.getId(), (msgx,typex)->{
								if(typex==MsgType.AT) {
									ListenerUtils.releaseListener(getId());
									game.skipWait(WaitReason.DieWord);
								}
							});
						hasGun=false;
						game.logger.logSkill(this,p,"猎人杀死");
						super.sendPrivate("你杀死了"+p.getMemberString());
						boolean hasDarkWolf=false;
						for(Villager vill:game.playerlist) {
							if(vill instanceof DarkWolf) {
								hasDarkWolf=true;
								break;
							}
						}
						if(!hasDarkWolf)
							super.sendPublic(new PlainText("死亡，身份是猎人，同时带走了").plus(p.getAt()));
						else
							super.sendPublic(new PlainText("死亡，同时带走了").plus(p.getAt()));
						if(dir==DiedReason.Vote||dir==DiedReason.Explode) {
							game.logger.logDeath(p,DiedReason.Hunter);
							game.getScheduler().execute(()->p.onDied(DiedReason.Hunter));
						}else {
							p.isDead=true;
							game.kill(p,DiedReason.Hunter);
						}
					}catch(Throwable t) {
						super.sendPrivate("发生错误，正确格式为：“杀死 qq号或者游戏号码”！");
					}
				}
			});

			return true;
		}
		
		return false;
	}
	@Override
	public void onTurn() {
		onDiePending(dr);
	}
	@Override
	public Fraction getFraction() {
		return Fraction.God;
	}
	public Hunter(WerewolfGame werewolfGame, Member member) {
		super(werewolfGame, member);
	}
	@Override
	public int getTurn() {
		return 3;
	}
	@Override
	public String getRole() {
		return "猎人";
	}
}
