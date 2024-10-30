package com.khjxiaogu.TableGames.game.kalah;

import java.util.function.Function;

public class AIKalahBoard extends KalahBoard {


	public AIKalahBoard( int counth, int initc) {
		super("Simulated",null, counth, initc);
	}

	public static class ComputeResult {
		int slot;
		int point;

		public ComputeResult(int slot, int point) {
			super();
			this.slot = slot;
			this.point = point;
		}
	}

	@Override
	int input(boolean isFirst) {
		return 0;
	}
	// 计算两回合得分期望最高的值
	ComputeResult calculateBestResult(int slot) {
		return calculateResult(slot,this::findmax);
	}
	// 计算两回合得分期望平等的值
	ComputeResult calculateZeroResult(int slot) {
		return calculateResult(slot,this::findzero);
	}
	ComputeResult calculateResult(int slot,Function<int[],Integer> determine) {
		if (!canOperate()) {// 如果导致立即胜利或者立即失败，加极大期望
			if (checkWin() >= 0)
				return new ComputeResult(slot, 1000);
			return new ComputeResult(slot, -1000);
		}
		int[] pts=calculate();
		slot = determine.apply(pts);
		return new ComputeResult(slot, pts[slot]);
	}
	int[] calculate() {
		short[] data = new short[COUNT_HOLE * 2 + 3];
		short[] data2 = new short[COUNT_HOLE * 2 + 3];
		int[] pts = new int[COUNT_HOLE];
		int s2 = 0;
		this.save(data);
		int origscore = this.getScore();
		for (int i = 0; i < COUNT_HOLE; i++) {
			this.load(data);
			if (!this.movable(i + 1)) {// 此格不能走子，加最大负期望
				pts[i] = -10000;
				continue;
			}

			int nxt = this.move(i + 1);
			pts[i] = this.getScore() - origscore;
			if (nxt == 1) {
				pts[i] += this.calculateOrigBestResult(s2).point;// 如果还能继续下子，继续计算。
			}
			this.save(data2);// 计算对方走子最高期望
			this.loadReverse(data2);
			pts[i] -= this.calculateOrigBestResult(s2).point;// 计算本操作得分综合期望
		}
		return pts;
	}
	// 计算一回合得分期望最高的值
	ComputeResult calculateOrigBestResult(int slot) {
		short[] data = new short[COUNT_HOLE * 2 + 3];
		int[] pts = new int[COUNT_HOLE];
		int s2 = 0;
		this.save(data);
		int origscore = this.getScore();
		if (!canOperate()) {
			if (checkWin() >= 0)
				return new ComputeResult(slot, 1000);
			return new ComputeResult(slot, -1000);
		}
		for (int i = 0; i < COUNT_HOLE; i++) {
			this.load(data);
			if (!this.movable(i + 1)) {
				pts[i] = -10000;
				continue;
			}
			int nxt = this.move(i + 1);
			pts[i] = this.getScore() - origscore;
			if (nxt == 1) {
				pts[i] += this.calculateOrigBestResult(s2).point;
			}
		}
		slot = findmax(pts);
		return new ComputeResult(slot, pts[slot]);
	}

	// 计算最大期望值
	int findmax(int a[]) {
		int max = a[COUNT_HOLE - 1];
		int index = COUNT_HOLE - 1;
		for (int i = COUNT_HOLE - 2; i >= 0; i--) {
			if (a[i] > max) {
				max = a[i];
				index = i;
			}
		}
		return index;
	}
	int findmin(int a[]) {
		int min = a[COUNT_HOLE - 1];
		int index = COUNT_HOLE - 1;
		for (int i = COUNT_HOLE - 2; i >= 0; i--) {
			if(a[i]<-1000)continue;
			if (a[i] <min) {
				min = a[i];
				index = i;
			}
		}
		return index;
	}
	int findzero(int a[]) {
		int max=Integer.MAX_VALUE;
		int index = COUNT_HOLE - 1;
		for (int i = COUNT_HOLE - 1; i >= 0; i--) {
			if(a[i]<-1000)continue;
			if (Math.abs(a[i]) < max) {
				if(max==0&&Math.random()>0.5)continue;
				max = a[i];
				index = i;
			}
		}
		return index;
	}
}
