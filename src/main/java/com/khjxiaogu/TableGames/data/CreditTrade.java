package com.khjxiaogu.TableGames.data;

import java.util.ArrayList;

import com.khjxiaogu.TableGames.platform.GlobalMain;

public class CreditTrade {
	int pt;
	String itm;
	String dn;
	int cnt;
	public static ArrayList<CreditTrade> trades=new ArrayList<>();
	static {
		new CreditTrade(3,"狼人杀vip券",1);
		new CreditTrade(10,"狼人杀vip券",4);
		new CreditTrade(20,"狼人杀vip券",9);
		new CreditTrade(30,"狼人杀vip券",15);
	}
	public CreditTrade(int pt, String itm, int cnt) {
		this.pt = pt;
		this.itm = itm;
		dn=itm;
		this.cnt = cnt;
		CreditTrade.trades.add(this);
	}
	public CreditTrade(int pt, String itm,String dn, int cnt) {
		this.pt = pt;
		this.itm = itm;
		this.dn=dn;
		this.cnt = cnt;
		CreditTrade.trades.add(this);
	}
	public boolean execute(long qq) {
		PlayerCredit pcd=GlobalMain.credit.get(qq);
		if(pcd.withdrawPT(pt)>=0) {
			pcd.giveItem(itm, cnt);
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		StringBuilder sb=new StringBuilder(dn);
		if(cnt>1) {
			sb.append("x").append(cnt);
		}
		sb.append(":").append(pt).append("积分");
		return sb.toString();
	}
	public static String getList() {
		StringBuilder ips=new StringBuilder();
		int i=0;
		for(CreditTrade ct:CreditTrade.trades) {
			ips.append(++i).append("、").append(ct.toString()).append("\n");
		}
		ips.append("输入@我 购买 序号 来购买物品");
		return ips.toString();
	}
}
