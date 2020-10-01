package com.khjxiaogu.TableGames.undercover;

import com.khjxiaogu.TableGames.Utils;

public class UnderCoverPlayerData {
	int wins;
	int loses;
	int winasspy;
	int loseasspy;
	int winasanno;
	int loseasanno;
	int aliveasanno;
	int aliveasspy;
	int alive;
	int outasanno;
	int outasspy;
	int out;
	int total;
	public UnderCoverPlayerData() {
	}
	public void log(boolean isSpy,boolean Spywin,boolean isOut) {
		total++;
		if(isOut)
			out++;
		else
			alive++;
		if(isSpy) {
			if(Spywin) {
				winasspy++;
				wins++;
			}else {
				loseasspy++;
				loses++;
			}
			if(isOut)
				outasspy++;
			else
				aliveasspy++;
		}else {
			if(Spywin) {
				loseasanno++;
				loses++;
			}else {
				winasanno++;
				wins++;
			}
			if(isOut)
				outasanno++;
			else
				aliveasanno++;
		}
	}
	@Override
	public String toString() {
		StringBuilder apd=new StringBuilder();
		apd.append("谁是卧底统计数据").append("\n");
		apd.append("总计场数 ").append(total).append("\n");
		apd.append("总计胜率 ").append(Utils.percent(wins,total)).append("\n");
		apd.append("卧底胜率 ").append(Utils.percent(winasspy,total)).append("\n");
		apd.append("平民胜率 ").append(Utils.percent(winasanno,total)).append("\n");
		apd.append("总计存活率 ").append(Utils.percent(alive,total)).append("\n");
		apd.append("卧底存活率 ").append(Utils.percent(aliveasspy,total)).append("\n");
		apd.append("平民存活率 ").append(Utils.percent(aliveasanno,total)).append("\n");
		return apd.toString();
	}
}
