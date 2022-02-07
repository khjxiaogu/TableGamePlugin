/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.fastclue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.UserFunction;
import com.khjxiaogu.TableGames.utils.Utils;

public class CluePlayer extends UserFunction{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2373176694812777919L;
	FastClueGame game;
	CluePlayer next;
	Set<Card> alknow=new HashSet<>();
	List<Card> inhand=new ArrayList<>();
	Card toshow;
	boolean isDead;
	boolean zqqr=false;
	public CluePlayer(FastClueGame game,AbstractUser mem) {
		super(mem);
		this.game=game;
	}
	public CluePlayer(FastClueGame game,int id) {
		super(GlobalMain.createBot(id,BotCluePlayer.class,game));
		this.game = game;
	}
	public void addCard(Card cd) {
		inhand.add(cd);
		alknow.add(cd);
	}
	public void addKnow(Card cd) {
		alknow.add(cd);
	}
	public void onGameStart() {
		StringBuilder sb=new StringBuilder("手中卡片：");
		for(Card c:inhand) {
			sb.append("\n").append(c.getDisplayName());
		}
		sendPrivate(sb.toString());
	}
	public void onTurn() {
		if(isDead) {
			next.onTurn();
			return;
		}
		if(game.CheckWin())
			return;
		zqqr=false;
		this.sendPublic("的回合。");
		sendPrivate(game.getCardList());
		sendPrivate("你可以提出假设：“假设 [凶手角色号码] [房间号码] [凶器号码]”。\n可以提出指控“指控 [凶手角色号码] [房间号码] [凶器号码]”。\n可以输入“我的卡片”查看手中的卡片。\n可以输入“已知卡片”查看所有我看过的卡片。\n输入“放弃”放弃当前回合。\n你有2分钟的时间考虑。");
		super.registerListener((msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String text=msg.getText();
			if(text.startsWith("我的卡片")) {
				StringBuilder sb=new StringBuilder("手中卡片：");
				for(Card c:inhand) {
					sb.append("\n").append(c.getDisplayName());
				}
				sendPrivate(sb.toString());
			}else if(text.startsWith("已知卡片")){
				StringBuilder sb=new StringBuilder("已知卡片：");
				for(Card c:alknow) {
					sb.append("\n").append(c.getDisplayName());
				}
				sendPrivate(sb.toString());
			}else if(text.startsWith("假设")) {
				try {
					String ut=Utils.removeLeadings("假设",text);
					int rolei=Integer.parseInt(ut.substring(0,1));
					String raw=ut.substring(1).trim();
					int roomi=Integer.parseInt(raw.substring(0,1));
					int weaponi=Integer.parseInt(raw.substring(1).trim());
					Card weapon=game.getWeapon(weaponi);
					Card role=game.getRole(rolei);
					Card room=game.getRoom(roomi).present;
					this.sendPublic("假设 "+role.getName()+" 在 "+room.getName()+" 使用 "+weapon.getName()+" 杀人。\n请等待15秒检查玩家卡片。");
					super.releaseListener();
					game.doPrompt.terminateWait();
					game.getScheduler().submit(()->checkAllCardPresnet(room,weapon,role));
				}catch(Exception e) {
					sendPrivate("格式错误，正确格式：“假设 [凶手角色号码] [房间号码] [凶器号码]”");
				}
			}else if(text.startsWith("指控")) {
				if(!zqqr) {
					zqqr=true;
					sendPrivate("你确定你要指控吗？一旦错误将立即出局！如果确定，请重新输入一次。");
					return;
				}
				try {
					String ut=Utils.removeLeadings("指控",text);
					int rolei=Integer.parseInt(ut.substring(0,1));
					String raw=ut.substring(1).trim();
					int roomi=Integer.parseInt(raw.substring(0,1));
					int weaponi=Integer.parseInt(raw.substring(1).trim());
					Card weapon=game.getWeapon(weaponi);
					Card role=game.getRole(rolei);
					Card room=game.getRoom(roomi).present;
					this.sendPublic("指控 "+role.getName()+" 在 "+room.getName()+" 使用 "+weapon.getName()+" 杀人。");
					super.releaseListener();
					game.doPrompt.terminateWait();
					if(room==game.Rroom&&role==game.Rrole&&weapon==game.Rweapon) {
						this.sendPublic("指控正确！");
						game.Win(this);
					}else {
						isDead=true;
						StringBuilder sb=new StringBuilder("指控错误，出局！\n手中卡片：");
						for(Card c:inhand) {
							sb.append("\n").append(c.getDisplayName());
							game.addKnow(c);
						}
						this.sendPublic(sb.toString());
						game.getScheduler().submit(next::onTurn);
					}
				}catch(Exception e) {
					sendPrivate("格式错误，正确格式：“指控 [凶手角色号码] [房间号码] [凶器号码]”");
				}
			}else if(text.startsWith("放弃")) {
				sendPrivate("已经放弃");
				game.doPrompt.stopWait();
			}
		});
		try {
			game.doPrompt.startWait(120000);
		}catch(RuntimeException ex) {
			return;
		}
		super.releaseListener();
		game.getScheduler().submit(next::onTurn);
	}

	public void checkAllCardPresnet(Card room,Card weapon,Card role) {
		CluePlayer fst=this;
		Card rslt=null;
		while(rslt==null&&fst.next!=this) {
			fst=fst.next;
			rslt=fst.checkCardPresent(room, weapon, role);
		}
		if(rslt==null) {
			game.selectCard.startWait(15000);
			game.sendPublicMessage("无玩家出示卡片");
		}else {
			alknow.add(rslt);
			sendPrivate(fst.getNameCard()+" 向你出示了\n"+rslt.getDisplayName());
			game.sendPublicMessage(fst.getNameCard()+" 向 "+getNameCard()+" 出示了一张牌");
		}
		game.getScheduler().submit(next::onTurn);
	}
	public Card checkCardPresent(Card room,Card weapon,Card role) {
		List<Card> cl=new ArrayList<>(3);
		if(inhand.contains(weapon)) {
			cl.add(weapon);
		}
		if(inhand.contains(room)) {
			cl.add(room);
		}
		if(inhand.contains(role)) {
			cl.add(role);
		}
		if(isDead) {
			if(cl.size()>0)
				return cl.get(0);
			return null;
		}
		if(cl.size()==1) {
			sendPrivate("你拥有一张对应的卡，将在15秒后出示。");
			game.selectCard.startWait(15000);
			return cl.get(0);
		}
		if(cl.size()>1) {
			toshow=cl.get(0);
			StringBuilder sb=new StringBuilder("你有以下卡片符合要求：");
			int i=0;
			for(Card c:cl) {
				sb.append("\n").append(i++).append("、").append(c.getDisplayName());
			}
			sb.append("\n你有15秒时间选择要出示的卡片，输入“出示 [卡片号码]”出示对应卡片，过时自动出示第一张。");
			sendPrivate(sb.toString());
			super.registerListener((msg,type)->{
				if(type!=MsgType.PRIVATE)return;
				String text=msg.getText();
				if(text.startsWith("出示")) {
					try {
						int cd=Integer.parseInt(Utils.removeLeadings("出示",text));
						toshow=cl.get(cd);
						game.selectCard.stopWait();
						sendPrivate("已选择卡片。");
					}catch(Exception e) {
						sendPrivate("格式错误，正确格式：“出示 [卡片号码]”");
					}
				}
			});
			game.selectCard.startWait(15000);
			super.releaseListener();
			return toshow;
		}
		return null;
	}
}
