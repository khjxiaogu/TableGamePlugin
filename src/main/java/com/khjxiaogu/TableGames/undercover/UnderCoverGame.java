package com.khjxiaogu.TableGames.undercover;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.khjxiaogu.TableGames.Game;
import com.khjxiaogu.TableGames.Utils;
import com.khjxiaogu.TableGames.VoteUtil;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary.WordPair;
import com.khjxiaogu.TableGames.MessageListener.MsgType;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

public class UnderCoverGame extends Game {
	
	boolean isEnded;
	List<UCPlayer> innos=Collections.synchronizedList(new ArrayList<>());
	int spycount=1;
	Integer cplayer;
	Thread main=new Thread(()->gameMain());
	VoteUtil<UCPlayer> vu=new VoteUtil<>();
	List<Boolean> wds=Collections.synchronizedList(new ArrayList<>());
	public UnderCoverGame(Group group, int cplayer) {
		super(group, cplayer,2);
		this.cplayer=cplayer;
		wds.add(true);
		if(cplayer>7) {
			spycount=2;
			wds.add(true);
		}
		int innocount=cplayer-spycount;
		for(int i=0;i<innocount;i++) {
			wds.add(false);
		}
		Collections.shuffle(wds);
	}
	@Override
	public void forceStop() {
		doFinalize();
		main.stop();
	}
	@Override
	protected void doFinalize() {
		for(UCPlayer in:innos)
			Utils.RemoveMember(in.member.getId());
		super.doFinalize();
	}
	public UCPlayer getPlayerById(long id) {
		for(UCPlayer p:innos) {
			if(p.member.getId()==id)
				return p;
		}
		return null;
	}
	@Override
	public boolean addMember(Member mem) {
		if(this.getPlayerById(mem.getId())!=null) {
			this.sendPublicMessage(new At(mem).plus("你已经报名了！"));
			return false;
		}
		if(!Utils.tryAddMember(mem.getId())) {
			this.sendPublicMessage(new At(mem).plus("你已参加其他游戏！"));
			return true;
		}
		mem.sendMessage("报名成功！");
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
		while(true) {
			if(changeWordNeeded) {
				changeWordNeeded=false;
				WordPair wp=UnderCoverTextLibrary.getRandomPair();
				for(UCPlayer in:innos) {
					if(!in.isDead)
						in.onGameStart(wp);
				}
			}else
				changeWordNeeded=true;
			for(UCPlayer in:innos) {
				if(in.isDead)continue;
				in.sendPublic("请在1分钟内描述你的词语，可以随时@我结束描述");
				Utils.registerListener(in.member.getId(),group,(msg,type)->{
					if(type==MsgType.AT)
						main.interrupt();
				});
				try {Thread.sleep(60000);} catch (InterruptedException e) {}
				Utils.releaseListener(in.member.getId());
			}
			vu.clear();
			for(UCPlayer in:innos) {
				if(in.isDead)continue;
				vu.addToVote(in);
				Utils.registerListener(in.member.getId(),group,(msg,type)->{
					At at=msg.first(At.Key);
					if(at==null)return;
					String content=Utils.getPlainText(msg);
					if(content.startsWith("投票")) {
						UCPlayer p=getPlayerById(at.getTarget());
						if(p==null) {
							in.sendPublic("选择的玩家非游戏玩家，请重新输入");
							return;
						}
						Utils.releaseListener(in.member.getId());
						in.sendPublic(new MessageChainBuilder().append("已经投票给").append(at).asMessageChain());
						if(vu.vote(in,p))
							main.interrupt();
					}else if(type==MsgType.AT&&content.startsWith("弃权")) {
						Utils.releaseListener(in.member.getId());
						in.sendPublic("已弃权");
						vu.giveUp(in);
						if(vu.finished())
							main.interrupt();
					}
				});
			}
			this.sendPublicMessage("开始投票，请在两分钟内输入“投票 @要投的人”进行投票，或者 @我 弃权 弃票");
			vu.hintVote(scheduler);
			try {Thread.sleep(120000);;} catch (InterruptedException e) {}
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
				Utils.releaseListener(in.member.getId());
				if(!in.isDead) {
					left++;
					hasSpy|=in.isSpy;
				}
			}
			if(hasSpy) {
				if(left<=spycount+2) {
					this.isEnded=true;
					this.sendPublicMessage("卧底胜利！");
					break;
				}
			}else {
				this.isEnded=true;
				this.sendPublicMessage("卧底失败！");
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
	public boolean onReAttach(Long c) {
		return false;
	}

}
