package com.khjxiaogu.TableGames.clue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.khjxiaogu.TableGames.Player;
import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Member;

public class CluePlayer extends Player {
	Room current;
	ClueGame game;
	CluePlayer next;
	Set<Card> alknow=new HashSet<Card>();
	List<Card> inhand=new ArrayList<Card>();
	Card toshow;
	boolean isDead;
	boolean zqqr=false;
	public CluePlayer(ClueGame game,Member member) {
		super(member);
		this.game=game;
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
		this.sendPrivate(sb.toString());
	}
	public void onTurn() {
		if(isDead) {
			next.onTurn();
			return;
		}
		if(game.CheckWin())
			return;
		int dice=game.roll();
		int dix=dice;
		while(--dix>=0) {
			current=current.next;
		}
		zqqr=false;
		this.sendPublic("投出了 "+dice+" ，当前所在房间："+current.name);
		this.sendPrivate(game.getCardList());
		this.sendPrivate("你当前在 "+current.name);
		this.sendPrivate("你可以提出假设：“假设 [凶手角色ID] [凶器卡号码]”。\n可以提出指控“指控 [凶手角色ID] [凶器卡号码]”。\n可以输入“我的卡片”查看手中的卡片。\n可以输入“已知卡片”查看所有我看过的卡片。\n输入“放弃”放弃当前回合。\n你有2分钟的时间考虑。");
		ListenerUtils.registerListener(this.getId(),(msg,type)->{
			if(type!=MsgType.PRIVATE)return;
			String text=Utils.getPlainText(msg);
			if(text.startsWith("我的卡片")) {
				StringBuilder sb=new StringBuilder("手中卡片：");
				for(Card c:inhand) {
					sb.append("\n").append(c.getDisplayName());
				}
				this.sendPrivate(sb.toString());
			}else if(text.startsWith("已知卡片")){
				StringBuilder sb=new StringBuilder("已知卡片：");
				for(Card c:alknow) {
					sb.append("\n").append(c.getDisplayName());
				}
				this.sendPrivate(sb.toString());
			}else if(text.startsWith("假设")) {
				try {
					String ut=Utils.removeLeadings("假设",text);
					int rolei=Integer.parseInt(ut.substring(0,1));
					int weaponi=Integer.parseInt(ut.substring(1).trim());
					Card weapon=game.getWeapon(weaponi);
					Card role=game.getRole(rolei);
					Card room=current.present;
					this.sendPublic("假设 "+role.getName()+" 在 "+room.getName()+" 使用 "+weapon.getName()+" 杀人。\n请等待15秒检查玩家卡片。");
					ListenerUtils.releaseListener(this.getId());
					game.doPrompt.terminateWait();
					game.getScheduler().submit(()->checkAllCardPresnet(room,weapon,role));
				}catch(Exception e) {
					this.sendPrivate("格式错误，正确格式：“假设 [凶手角色ID] [凶器卡号码]”");
				}
			}else if(text.startsWith("指控")) {
				if(!zqqr) {
					zqqr=true;
					this.sendPrivate("你确定你要指控吗？一旦错误将立即出局！如果确定，请重新输入一次。");
					return;
				}
				try {
					String ut=Utils.removeLeadings("指控",text);
					int rolei=Integer.parseInt(ut.substring(0,1));
					int weaponi=Integer.parseInt(ut.substring(1).trim());
					Card weapon=game.getWeapon(weaponi);
					Card role=game.getRole(rolei);
					Card room=current.present;
					this.sendPublic("指控 "+role.getName()+" 在 "+room.getName()+" 使用 "+weapon.getName()+" 杀人。");
					ListenerUtils.releaseListener(this.getId());
					game.doPrompt.terminateWait();
					if(room==game.Rroom&&role==game.Rrole&&weapon==game.Rweapon) {
						this.sendPublic("指控正确！");
						game.Win(this);
					}else {
						this.isDead=true;
						StringBuilder sb=new StringBuilder("指控错误，出局！\n手中卡片：");
						for(Card c:inhand) {
							sb.append("\n").append(c.getDisplayName());
							game.addKnow(c);
						}
						this.sendPublic(sb.toString());
						game.getScheduler().submit(()->next.onTurn());
					}
				}catch(Exception e) {
					this.sendPrivate("格式错误，正确格式：“假设 [凶手角色ID] [凶器卡号码]”");
				}
			}else if(text.startsWith("放弃")) {
				this.sendPrivate("已经放弃");
				game.doPrompt.stopWait();
			}
		});
		try {
			game.doPrompt.startWait(120000);
		}catch(RuntimeException ex) {
			return;
		}
		ListenerUtils.releaseListener(this.getId());
		game.getScheduler().submit(()->next.onTurn());
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
			this.sendPrivate(fst.getNameCard()+" 向你出示了\n"+rslt.getDisplayName());
			game.sendPublicMessage(fst.getNameCard()+" 向 "+this.getNameCard()+" 出示了一张牌");
		}
		game.getScheduler().submit(()->next.onTurn());
	}
	public Card checkCardPresent(Card room,Card weapon,Card role) {
		List<Card> cl=new ArrayList<>(3);
		if(inhand.contains(weapon))
			cl.add(weapon);
		if(inhand.contains(room))
			cl.add(room);
		if(inhand.contains(role))
			cl.add(role);
		if(isDead) {
			if(cl.size()>0) {
				return cl.get(0);
			}
			return null;
		}
		if(cl.size()==1) {
			this.sendPrivate("你拥有一张对应的卡，将在15秒后出示。");
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
			this.sendPrivate(sb.toString());
			ListenerUtils.registerListener(this.getId(),(msg,type)->{
				if(type!=MsgType.PRIVATE)return;
				String text=Utils.getPlainText(msg);
				if(text.startsWith("出示")) {
					try {
						int cd=Integer.parseInt(Utils.removeLeadings("出示",text));
						toshow=cl.get(cd);
						game.selectCard.stopWait();
						this.sendPrivate("已选择卡片。");
					}catch(Exception e) {
						this.sendPrivate("格式错误，正确格式：“出示 [卡片号码]”");
					}
				}
			});
			game.selectCard.startWait(15000);
			ListenerUtils.releaseListener(this.getId());
			return toshow;
		}
		return null;
	}
}
