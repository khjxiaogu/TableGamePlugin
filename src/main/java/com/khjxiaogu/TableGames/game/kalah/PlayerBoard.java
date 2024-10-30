package com.khjxiaogu.TableGames.game.kalah;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.utils.WaitThread;

public class PlayerBoard extends KalahBoard {
	AbstractUser user;
	AIKalahBoard checker;
	int reply;
	boolean exitQuery;
	WaitThread wt = new WaitThread();
	String rank;
	public PlayerBoard(AbstractUser au,int h,int c) {
		super(au.getMemberString(),au.getRoom(),h,c);
		this.user = au;
		checker=new AIKalahBoard(h,c);
	}

	@Override
	int input(boolean isFirst) {
		String in=output();
		ar.sendMessage(Utils.sendTextAsImage(in, ar));
		reply = 0;
		exitQuery = false;
		user.sendForName((isFirst?"您先手":"")+
				"，请输入你要选择的棋盘序号(1-" + COUNT_HOLE + ")，输入" + (COUNT_HOLE + 1) + "或者2分钟未选择自动退出：");
		user.registerListener((m, t) -> {
			String text = m.getText();
			try {
				int num = Integer.parseInt(text);
				if (num < 1 || num > COUNT_HOLE + 1) {
					user.sendForName("非法的输入！请输入1-" + (COUNT_HOLE + 1) + "范围内的正整数");
					return;
				} else if (num == COUNT_HOLE + 1) {
					if (!exitQuery) {
						user.sendForName("确定要退出吗？退出将强行结算分数。再次输入" + (COUNT_HOLE + 1) +"表示确定。");
						exitQuery=true;
						return;
					}
					reply = 0;
				} else if (!movable(num)) {
					user.sendForName("该棋盘中棋子数为0，请重新选择！");
					return;
				} else {
					reply = num;
					short[] data=new short[COUNT_HOLE * 2 + 3];
					this.save(data);
					checker.load(data);
					int[] pts=checker.calculate();
					int best=checker.findmax(pts);
					int worst=checker.findmin(pts);
					if(pts[num-1]==pts[best]) 
						rank="妙手";
					else if(pts[num-1]==pts[worst]) 
						rank="俗手";
					else
						rank="本手";
					
				}
				wt.stopWait();
			}catch(NumberFormatException nce) {
				return;
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
		});
		wt.startWait(60 * 2 * 1000);
		return reply;
	}

	String output() {
        //输出对方玩家基地中棋子数
		StringBuilder sb=new StringBuilder("对方基地：");
        for (int i = COUNT_HOLE*2-1; i > COUNT_HOLE-1; i--)
        	sb.append(String.format("%2s",String.valueOf(pits[i].count))).append(" ");
        sb.append("\n积分区域：").append(String.format("%2s",String.valueOf(score2.count))).append(" "+"   ".repeat(COUNT_HOLE-2)).append(String.format("%2s",String.valueOf(score.count)));
        sb.append("\n己方基地：");
         
        //输出当前玩家基地中棋子数
        for (int i = 0; i < COUNT_HOLE; i++)
        	sb.append(String.format("%2s",String.valueOf(pits[i].count))).append(" ");
        sb.append("\n棋洞编号：");
        
        for (int i = 1; i < COUNT_HOLE+1; i++)//输出格子序号
        	sb.append(String.format("%2s",String.valueOf(i))).append(" ");
        return sb.toString();
    }

	@Override
	void announcePlayed(int pit) {
		super.announce(getName()+"移动了洞"+pit+"，"+rank);
	}
}
