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
package com.khjxiaogu.TableGames.game.clue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.khjxiaogu.TableGames.game.clue.Card.CardType;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.WaitThread;


public class ClueGame extends Game {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8126731305809945361L;
	Random rnd=new Random();
	List<Card> weapons=new ArrayList<>();//凶器卡
	List<CluePlayer> players=new ArrayList<>();//玩家
	List<RoleCard> roles=new ArrayList<>();//角色卡
	List<Room> rooms=new ArrayList<>();//房间
	WaitThread selectCard=new WaitThread();//选择出示等待
	WaitThread doPrompt=new WaitThread();//假设等待
	String[] roomnames= new String[]{"客厅","厨房","餐厅","卧室","书房","阳台","休息室","舞厅","台球室"};
	String[] weaponnames=new String[] {"烛台","小刀","铅管","手枪","绳子","扳手"};
	List<Card> allcard=new ArrayList<>();//玩家抽卡
	Card Rroom;//正确房间
	Card Rweapon;//正确武器
	Card Rrole;//正确角色
	boolean alive=true;//游戏是否存活
	int cpp;//每个玩家分到卡片数量
	int tcp;//总玩家数
	public ClueGame(AbstractRoom group, int cplayer) {
		super(group, cplayer,4);
		tcp=cplayer;
		List<Card> roomcard=new ArrayList<>();
		Room last=null;
		int is=0;
		for(String room:roomnames) {
			Room e=new Room(room,is++);
			if(last!=null) {
				last.next=e;
			}
			last=e;
			rooms.add(e);
			roomcard.add(e.present);
		}
		last.next=rooms.get(0);
		Collections.shuffle(roomcard);
		Rroom=roomcard.remove(0);
		allcard.addAll(roomcard);


		List<Card> rolecard=new ArrayList<>();
		for(int i=0;i<cplayer;i++) {
			RoleCard rc=new RoleCard(this,i);
			roles.add(rc);
			rolecard.add(rc);
		}
		Collections.shuffle(rolecard);
		Rrole=rolecard.remove(0);
		allcard.addAll(rolecard);

		is=0;
		List<Card> weaponcard=new ArrayList<>();
		for(String weapon:weaponnames) {
			Card wp=new Card(weapon,is++,CardType.Weapon);
			weaponcard.add(wp);
			weapons.add(wp);
		}
		Collections.shuffle(weaponcard);
		Rweapon=weaponcard.remove(0);
		allcard.addAll(weaponcard);
		cpp=allcard.size()/cplayer;
		Collections.shuffle(allcard);
	}

	@Override
	public boolean addMember(AbstractUser mem) {
		if(getPlayerById(mem.getId())!=null) {
			mem.sendPublic("你已经报名了！");
			return false;
		}
		if(!GameUtils.tryAddMember(mem.getId())) {
			mem.sendPublic("你已参加其他游戏！");
			return true;
		}
		if(roles.size()>0) {
			try {
				synchronized(players) {
					int min=players.size();
					CluePlayer cp=new CluePlayer(this,mem);
					players.add(cp);

					cp.sendPrivate("已经报名");
					String nc=cp.getNameCard();
					if(nc.indexOf('|')!=-1) {
						nc=nc.split("\\|")[1];
					}
					if(min!=0) {
						players.get(min-1).next=cp;
					}
					int cpx=cpp;
					while(--cpx>=0) {
						cp.addCard(allcard.remove(0));
					}
					cp.setNameCard(min+"号 |"+nc);
					if(tcp==players.size()) {
						cp.next=players.get(0);
						this.sendPublicMessage(getName()+"已满人，游戏即将开始。");
						getScheduler().execute(this::gameStart);
					}
				}
				return true;
			} catch (IllegalArgumentException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	public void gameStart() {
		while(allcard.size()>0) {
			players.get(rnd.nextInt(players.size())).addCard(allcard.remove(0));
		}
		for(CluePlayer p:players) {
			p.current=rooms.get(0);
			p.onGameStart();
		}
		players.get(0).onTurn();
	}
	private CluePlayer getPlayerById(UserIdentifier id) {
		for(CluePlayer p:players) {
			if(p.getId().equals(id))
				return p;
		}
		return null;
	}

	@Override
	public void forceStart() {
		this.sendPublicMessage("本游戏无法强行开始。");
	}

	@Override
	public String getName() {
		return "妙探寻凶";
	}

	@Override
	public boolean isAlive() {
		return alive;
	}

	@Override
	public boolean onReAttach(UserIdentifier id) {
		return false;
	}
	int roll() {
		return rnd.nextInt(6)+1;
	}
	String getCardList() {
		StringBuilder sb=new StringBuilder("所有凶器：");
		int i=0;
		for(Card weapon:weapons) {
			sb.append("\n").append(i++).append("、").append(weapon.getName());
		}
		sb.append("\n所有玩家：");
		for(CluePlayer player:players) {
			sb.append("\n").append(player.getNameCard());
		}
		return sb.toString();
	}
	public boolean CheckWin() {
		int np=0;
		CluePlayer alive = null;
		for(CluePlayer player:players) {
			if(!player.isDead) {
				np++;
				alive=player;
			}
		}
		if(np==1) {
			Win(alive);
			return true;
		}
		return false;
	}
	public void Win(CluePlayer cp) {
		StringBuilder result=new StringBuilder("胜利玩家：");
		result.append(cp.getNameCard());
		for(CluePlayer p:players) {
			result.append("\n").append(p.getNameCard()).append(p.isDead?"(出局)":"(在场)").append("持有卡片：");
			for(Card c:p.inhand) {
				result.append(c.getDisplayName()).append(" ");
			}
		}
		result.append("\n正确答案：").append(Rrole.getName()).append(" 在 ").append(Rroom.getName()).append(" 使用 ").append(Rweapon.getName()).append(" 杀人。");
		this.sendPublicMessage(new Image(Utils.textAsImage(result.toString())));
		doFinalize();
	}
	@Override
	protected void doFinalize() {
		alive=false;
		for(CluePlayer p:players) {
			p.releaseListener();
			GameUtils.RemoveMember(p.getId());
			String nc=p.getNameCard();
			if(nc.indexOf('|')!=-1) {
				nc=nc.split("\\|")[1];
			}
			p.setNameCard(nc);
		}
		super.doFinalize();
	}

	public Card getWeapon(int num) {
		return weapons.get(num);
	}
	@Override
	public void forceStop() {
		selectCard.terminateWait();
		doPrompt.terminateWait();
		StringBuilder result=new StringBuilder("游戏中断。");
		for(CluePlayer p:players) {
			result.append("\n").append(p.getNameCard()).append(p.isDead?"(出局)":"(在场)").append("持有卡片：");
			for(Card c:p.inhand) {
				result.append(c.getDisplayName()).append(" ");
			}
		}
		result.append("\n正确答案：").append(Rrole.getName()).append(" 在 ").append(Rroom.getName()).append(" 使用 ").append(Rweapon.getName()).append(" 杀人。");
		this.sendPublicMessage(new Image(Utils.textAsImage(result.toString())));
		doFinalize();
	}

	public Card getRole(int num) {
		return roles.get(num);
	}
	public void addKnow(Card c) {
		for(CluePlayer player:players) {
			player.addKnow(c);
		}
	}
	public CluePlayer getPlayer(int num) {
		return players.get(num);
	}
}
