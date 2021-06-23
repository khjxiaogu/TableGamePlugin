package com.khjxiaogu.TableGames.undercover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary.WordPair;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.VoteHelper;
import com.khjxiaogu.TableGames.utils.WaitThread;

public class UnderCoverHolder extends Game {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6609861527209746162L;
	boolean isEnded;
	List<UCPlayer> innos=Collections.synchronizedList(new ArrayList<>());
	int spycount=1;
	Integer cplayer;
	Thread main=new Thread(()->gameMain());
	VoteHelper<UCPlayer> vu=new VoteHelper<>();
	List<Boolean> wds=Collections.synchronizedList(new ArrayList<>());
	WaitThread wt=new WaitThread();
	double pointspool=0;
	public UnderCoverHolder(AbstractRoom group, int cplayer) {
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
		if(cplayer>11) {
			spycount=4;
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
	public UCPlayer getPlayerById(long id) {
		for(UCPlayer p:innos) {
			if(p.getId()==id)
				return p;
		}
		return null;
	}
	@Override
	public boolean addMember(AbstractPlayer mem) {
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
		WordPair wp=UnderCoverTextLibrary.getRandomPair();
		for(UCPlayer inx:innos) {
			if(inx.isDead) {
				continue;
			}
			inx.onGameStart(wp);
			getGroup().registerListener(inx.getId(),(msg,type)->{
				if(type==MsgType.AT) {
					if(Utils.getPlainText(msg).equals("出局")) {
						getGroup().releaseListener(inx.getId());
						inx.sendPublic("您已经出局");
						inx.isDead=true;
						boolean hasSpy=false;
						int left=0;
						for(UCPlayer in:innos) {
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
							}
						}else {
							isEnded=true;
							status="卧底失败！";
						}
						if(isEnded) {
							StringBuilder gr=new StringBuilder(status).append("\n游戏结果：");
							for(UCPlayer in:innos) {
								in.releaseListener();
								gr.append("\n").append(in.getMemberString()).append(in.isSpy?" 是卧底":" 不是卧底");
							}
							this.sendPublicMessage(Utils.sendTextAsImage(gr.toString(),getGroup()));
							return;
						}
					}else if(Utils.getPlainText(msg).equals("发词")) {
						WordPair wpx=UnderCoverTextLibrary.getRandomPair();
						for(UCPlayer in:innos) {
							if(!in.isDead) {
								in.onGameStart(wpx);
							}
						}
					}
				}
			});
		}


	}
	@Override
	public String getName() {
		return "谁是卧底发词";
	}

	@Override
	public boolean isAlive() {
		return !isEnded;
	}
	@Override
	public boolean onReAttach(Long c) {
		return false;
	}

}
