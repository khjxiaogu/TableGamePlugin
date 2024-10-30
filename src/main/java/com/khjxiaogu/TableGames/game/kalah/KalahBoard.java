package com.khjxiaogu.TableGames.game.kalah;

import java.util.Random;

import com.khjxiaogu.TableGames.platform.AbstractRoom;

public abstract class KalahBoard {
	public int COUNT_HOLE = 6;
	public short INITIAL_CHESS = 4;
	Pit[] pits ;// 全部游戏洞
	Pit score;// 玩家自己的积分洞
	Pit score2;// 对方玩家的积分洞
	String name;// 玩家的名字
	AbstractRoom ar;
	KalahBoard(String name,AbstractRoom ar,int counth,int initc) {
		this.COUNT_HOLE=counth;
		this.INITIAL_CHESS=(short) initc;
		this.ar=ar;
		this.name = name;
		this.pits= new Pit[COUNT_HOLE * 2];
		/*
		 * 11-10- 9- 8- 7- 6\
		 * (13)sc2| sc(12)
		 * 0- 1- 2- 3- 4- 5/
		 */
		for (int i = 0; i < COUNT_HOLE * 2; i++)
			pits[i] = new Pit(i);
		for (int i = 0; i < COUNT_HOLE * 2 - 1; i++)// 按照棋子摆放顺序依次链接所有棋洞
			pits[i].next = pits[i + 1];
		pits[COUNT_HOLE * 2 - 1].next = pits[0];// 首尾相连
		score = new Pit(COUNT_HOLE * 2);
		pits[COUNT_HOLE - 1].next = score;// 在链表内插入己方积分洞
		score.next = pits[COUNT_HOLE];
		score2 = new Pit(COUNT_HOLE * 2 + 1);
		for (int i = 0; i < COUNT_HOLE; i++) {// 指派各个棋洞对面的棋洞
			pits[i].opposite = pits[COUNT_HOLE * 2 - 1 - i];
			pits[COUNT_HOLE * 2 - 1 - i].opposite = pits[i];
		}
		reset();
	}

	/*
	 * 获取名字
	 */
	String getName() {
		return name;
	}

	/*
	 * 要求开始输入，返回1-7表示输入值，返回0表示请求了退出
	 */
	abstract int input(boolean isFirst);

	/*
	 * 检查棋子格是否可以被选中
	 */
	boolean movable(int pit) {
		return pits[pit - 1].count != 0;
	}

	/*
	 * 移动棋子
	 * 如果移动完成了，返回0
	 * 如果玩家需要继续操作，返回1
	 */
	int move(int pit) {
		Pit p = pits[pit - 1];
		int count = p.count;
		p.count = 0;
		// 棋子移动
		while (count-- != 0) {
			p = p.next;
			p.count++;
		}
		// 吃棋子判定
		if (p.count == 1 && p.position < COUNT_HOLE && p.position >= 0 && p.opposite.count > 0)// 如果最后放下的是己方洞
		{
			p.count = 0;// 取走自己身上的棋子
			score.count += p.opposite.count + 1;// 和对面的棋子一起放到己方洞
			p.opposite.count = 0;// 取走对面的棋子
		} else if (p.position == COUNT_HOLE * 2)// 最后放下的是己方积分洞，要求继续操作
			return 1;
		return 0;
	}

	void reset() {// 初始化游戏设定
		for (int i = 0; i < COUNT_HOLE * 2; i++) {
			pits[i].count = INITIAL_CHESS;
		}
		score.count = 0;
		score2.count = 0;
	}

	/*
	 * 检查是否应该结束游戏
	 */
	boolean canOperate() {
		if (score.count > COUNT_HOLE * INITIAL_CHESS || score2.count > COUNT_HOLE * INITIAL_CHESS)// 其中一位玩家持有一半以上棋子了
			return false;
		int opable = COUNT_HOLE;
		for (int i = 0; i < COUNT_HOLE; i++) {
			if (pits[i].count == 0)
				opable--;
		}
		if (opable == 0)
			return false;
		return true;
	}

	/*
	 * 保存棋盘游戏状态到数组
	 * 数组至少有COUNT_HOLE*2+2个内存空间
	 */
	void save(short[] data) {
		for (int i = 0; i < COUNT_HOLE * 2; i++)
			data[i] = pits[i].count;
		data[COUNT_HOLE * 2] = score.count;
		data[COUNT_HOLE * 2 + 1] = score2.count;
	}

	/*
	 * 从数组恢复棋盘游戏状态
	 * 数组至少有14个内存空间
	 */
	void load(short[] data) {
		for (int i = 0; i < COUNT_HOLE * 2; i++)
			pits[i].count = data[i];
		score.count = data[COUNT_HOLE * 2];
		score2.count = data[COUNT_HOLE * 2 + 1];
	}

	/*
	 * 从对方棋盘的数组恢复当前棋盘的游戏状态
	 * 数组至少有14个内存空间
	 */
	void loadReverse(short[] data) {
		for (int i = 0; i < COUNT_HOLE; i++) {// 交换双方阵列
			pits[i + COUNT_HOLE].count = data[i];
		}
		for (int i = COUNT_HOLE; i < COUNT_HOLE * 2; i++) {
			pits[i - COUNT_HOLE].count = data[i];
		}
		score.count = data[COUNT_HOLE * 2 + 1];
		score2.count = data[COUNT_HOLE * 2];
	}

	/*
	 * 检查是否符合胜利条件
	 * 如果为正数则是胜利
	 * 0则为平局
	 * 为负则是输了
	 */
	int win() {
		if (!canOperate())
			for (int i = COUNT_HOLE; i < COUNT_HOLE * 2; i++) {
				score2.count += pits[i].count;
			}
		return score.count - score2.count;
	}

	/*
	 * 检查是否符合胜利条件
	 * 如果为正数则是胜利
	 * 0则为平局
	 * 为负则是输了
	 */
	int checkWin() {
		int s2 = score2.count;
		int s1 = score.count;
		if (!canOperate())
			for (int i = COUNT_HOLE; i < COUNT_HOLE * 2; i++) {
				s2 += pits[i].count;
			}
		return s1 - s2;
	}

	int getScore() {
		return score.count;
	}

	int getScore2() {
		return score2.count;
	}
	void announce(String data) {
		ar.sendMessage(data);
	};
	void announcePlayed(int pit) {
		announce(getName()+"移动了洞"+pit);
	};
	public void host_game(KalahBoard other) {
	    boolean state = new Random().nextBoolean();//决定先手
	    boolean isFirst=true;
	    short[] data=new short[COUNT_HOLE * 2 + 3];//同步游戏数据用数组
	    int op;//玩家操作
	    KalahBoard cur=state?this:other;
	    KalahBoard opp=state?other:this;
	    while (true) {
	        int mstate;
	        while (true) {
	            if (!cur.canOperate()) {
	                mstate = 2;
	                break;
	            }
	            op = cur.input(isFirst);
	            if (op == 0) {
	                mstate = 2;
	                break;
	            }
	            mstate = cur.move(op);
	            cur.announcePlayed(op);
	            if (mstate != 1)
	                break;
	           // system("cls");
	        }
	        if (mstate == 2)
	            break;
	        isFirst=false;
	        cur.save(data);
	        //system("cls");
	        KalahBoard temp=cur;
	        cur=opp;
	        opp=temp;
	        cur.loadReverse(data);
	    }
	    int result = cur.win();
	    StringBuilder sb=new StringBuilder();
	    sb.append(cur.getName()).append("得分：").append(cur.getScore()).append("\n");
	    sb.append(opp.getName()).append("得分：").append(cur.getScore2()).append("\n");
	    if (result>0)
	        sb.append("恭喜").append(cur.getName()).append("游戏胜利。");
	    else if (result<0)
	    	sb.append("恭喜").append(opp.getName()).append("游戏胜利。");
	    else
	        sb.append("平局。");
	    sb.append("\n游戏结束。");
	    announce(sb.toString());
	}
}
