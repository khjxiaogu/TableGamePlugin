package com.khjxiaogu.TableGames.game.kalah;

public class Pit {
	Pit(int i) {
		this.position = (short) i;
	}

	Pit next;// 链表指示下一个棋洞
	Pit opposite;// 指示对面的棋洞
	short count;// 棋子数量
	short position;// 棋洞位置
}
