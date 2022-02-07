/**
 * Mirai Tablegames Plugin
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
package com.khjxiaogu.TableGames.game.undercover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.khjxiaogu.TableGames.data.PlayerDatabase.GameData;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary.WordPair;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.platform.message.At;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.VoteHelper;
import com.khjxiaogu.TableGames.utils.WaitThread;


public class UnderCoverGame extends Game {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7230131931848872043L;
	boolean isEnded;
	List<UCPlayer> innos=Collections.synchronizedList(new ArrayList<>());
	int spycount=1;
	Integer cplayer;
	Thread main=new Thread(this::gameMain);
	VoteHelper<UCPlayer> vu=new VoteHelper<>();
	List<Boolean> wds=Collections.synchronizedList(new ArrayList<>());
	WaitThread wt=new WaitThread();
	double pointspool=0;
	public UnderCoverGame(AbstractRoom group, int cplayer) {
		super(group, cplayer,2);
		this.cplayer=cplayer;
		wds.add(true);
		if(cplayer>7) {
			spycount=2;
			wds.add(true);
		}
		if(cplayer>9) {
			spycount=3;
			wds.add(true);
		}
		int innocount=cplayer-spycount;
		for(int i=0;i<innocount;i++) {
			wds.add(false);
		}
		Collections.shuffle(wds);
	}
	@SuppressWarnings("deprecation")
	@Override
	public void forceStop() {
		doFinalize();
		main.stop();
	}
	@Override
	protected void doFinalize() {
		isEnded=true;
		for(UCPlayer in:innos) {
			GameUtils.RemoveMember(in.getId());
		}
		super.doFinalize();
	}
	public UCPlayer getPlayerById(UserIdentifier id) {
		for(UCPlayer p:innos) {
			if(p.getId().equals(id))
				return p;
		}
		return null;
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
		mem.sendPrivate("报名成功！");
		synchronized(wds) {
			if(wds.size()<=0)return false;
			if(wds.remove(0)) {
				innos.add(new UCPlayer(mem,true));
			}else {
				innos.add(new UCPlayer(mem,false));
			}
			if(wds.size()==0) {
				startGame();
			}
			return true;
		}

	}

	private void startGame() {
		this.sendPublicMessage("谁是卧底游戏已满人，游戏即将开始！");
		main.start();
	}

	@Override
	public void forceStart() {
		startGame();
	}
	public void gameMain() {
		boolean changeWordNeeded=true;
		pointspool=innos.size()*0.5;
		while(true) {
			if(changeWordNeeded) {
				changeWordNeeded=false;
				WordPair wp=UnderCoverTextLibrary.getRandomPair();
				for(UCPlayer in:innos) {
					if(!in.isDead) {
						in.onGameStart(wp);
					}
				}
			} else {
				changeWordNeeded=true;
			}
			for(UCPlayer in:innos) {
				if(in.isDead) {
					continue;
				}
				in.sendPublic("请在1分钟内描述你的词语，可以随时@我结束描述");
				getGroup().registerListener(in.getId(),(msg,type)->{
					if(type==MsgType.AT) {
						wt.stopWait();
					}
				});
				wt.startWait(60000);
				getGroup().releaseListener(in.getId());
			}
			vu.clear();
			for(UCPlayer in:innos) {
				if(in.isDead) {
					continue;
				}
				vu.addToVote(in);
				getGroup().registerListener(in.getId(),(msg,type)->{
					At at=msg.first(At.class);
					if(at==null)return;
					String content=Utils.getPlainText(msg);
					if(content.startsWith("投票")) {
						UCPlayer p=getPlayerById(at.getTarget());
						if(p==null) {
							in.sendPublic("选择的玩家非游戏玩家，请重新输入");
							return;
						}
						getGroup().releaseListener(in.getId());
						in.sendPublic(new Text("已经投票给").asMessage().append(at));
						if(vu.vote(in,p)) {
							wt.stopWait();
						}
					}else if(type==MsgType.AT&&content.startsWith("弃权")) {
						getGroup().releaseListener(in.getId());
						in.sendPublic("已弃权");
						vu.giveUp(in);
						if(vu.finished()) {
							wt.stopWait();
						}
					}
				});
			}
			this.sendPublicMessage("开始投票，请在两分钟内输入“投票 @要投的人”进行投票，或者 @我 弃权 弃票");
			vu.hintVote(getScheduler());
			wt.startWait(120000);
			List<UCPlayer> vtd=vu.getMostVoted();
			vu.clear();
			if(vtd.size()!=1) {
				this.sendPublicMessage("同票，进入下一轮描述");
			}else {
				vtd.get(0).isDead=true;
				vtd.get(0).sendPublic("你被投出局了");
			}
			boolean hasSpy=false;
			int left=0;
			for(UCPlayer in:innos) {
				getGroup().releaseListener(in.getId());
				if(!in.isDead) {
					left++;
					hasSpy|=in.isSpy;
				}
			}
			String status="";
			if(hasSpy) {
				if(left<=spycount+2) {
					isEnded=true;
					status="卧底胜利！";
					GameData gd=GlobalMain.db.getGame(getName());
					double ppp=pointspool/spycount;
					for(UCPlayer in:innos) {
						UnderCoverPlayerData ucpd=gd.getPlayer(in.getId(),UnderCoverPlayerData.class);
						ucpd.log(in.isSpy,true,in.isDead);
						GlobalMain.credit.get(in.getId()).givePT(ppp);
						gd.setPlayer(in.getId(),ucpd);
					}
				}
			}else {
				isEnded=true;
				status="卧底失败！";
				GameData gd=GlobalMain.db.getGame(getName());
				double ppp=pointspool/(cplayer-spycount);
				for(UCPlayer in:innos) {
					UnderCoverPlayerData ucpd=gd.getPlayer(in.getId(),UnderCoverPlayerData.class);
					ucpd.log(in.isSpy,false,in.isDead);
					GlobalMain.credit.get(in.getId()).givePT(ppp);
					gd.setPlayer(in.getId(),ucpd);
				}
			}
			if(isEnded) {
				StringBuilder gr=new StringBuilder(status).append("\n游戏结果：");
				for(UCPlayer in:innos) {
					gr.append("\n").append(in.getMemberString()).append(in.isSpy?" 是卧底":" 不是卧底");
				}
				this.sendPublicMessage(Utils.sendTextAsImage(gr.toString(),getGroup()));
				break;
			}
		}
	}
	@Override
	public String getName() {
		return "谁是卧底";
	}

	@Override
	public boolean isAlive() {
		return !isEnded;
	}
	@Override
	public boolean onReAttach(UserIdentifier c) {
		return false;
	}

}
