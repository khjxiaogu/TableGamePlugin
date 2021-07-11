package com.khjxiaogu.TableGames.game.undercover;

import com.khjxiaogu.TableGames.data.GenericPlayerData;
import com.khjxiaogu.TableGames.utils.Utils;

public class UnderCoverPlayerData implements GenericPlayerData<UnderCoverPlayerData>{
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
		if(isOut) {
			out++;
		} else {
			alive++;
		}
		if(isSpy) {
			if(Spywin) {
				winasspy++;
				wins++;
			}else {
				loseasspy++;
				loses++;
			}
			if(isOut) {
				outasspy++;
			} else {
				aliveasspy++;
			}
		}else {
			if(Spywin) {
				loseasanno++;
				loses++;
			}else {
				winasanno++;
				wins++;
			}
			if(isOut) {
				outasanno++;
			} else {
				aliveasanno++;
			}
		}
	}
	@Override
	public String toString() {
		StringBuilder apd=new StringBuilder();
		apd.append("谁是卧底统计数据").append("\n");
		apd.append("总计场数 ").append(total).append("\n");
		apd.append("总计胜率 ").append(Utils.percent(wins,total)).append("\n");
		apd.append("卧底胜率 ").append(Utils.percent(winasspy,winasspy+loseasspy)).append("\n");
		apd.append("平民胜率 ").append(Utils.percent(winasanno,winasanno+loseasanno)).append("\n");
		apd.append("总计存活率 ").append(Utils.percent(alive,total)).append("\n");
		apd.append("卧底存活率 ").append(Utils.percent(aliveasspy,aliveasspy+outasspy)).append("\n");
		apd.append("平民存活率 ").append(Utils.percent(aliveasanno,outasanno)).append("\n");
		return apd.toString();
	}
	@Override
	public void plus(UnderCoverPlayerData another) {
		wins+=another.wins;
		loses+=another.loses;
		winasspy+=another.winasspy;
		loseasspy+=another.loseasspy;
		winasanno+=another.winasanno;
		loseasanno+=another.loseasanno;
		aliveasanno+=another.aliveasanno;
		aliveasspy+=another.aliveasspy;
		alive+=another.alive;
		outasanno+=another.outasanno;
		outasspy+=another.outasspy;
		out+=another.out;
		total=Math.max(another.total,total);
	}
}
