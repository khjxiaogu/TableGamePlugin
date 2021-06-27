package com.khjxiaogu.TableGames.data;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import com.khjxiaogu.TableGames.platform.mirai.MiraiMain;
import com.khjxiaogu.TableGames.utils.ImagePrintStream;

public class CreditTrade {
	int pt;
	String itm;
	String dn;
	int cnt;
	public static ArrayList<CreditTrade> trades=new ArrayList<>();
	static {
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
		PlayerCredit pcd=MiraiMain.credit.get(qq);
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
	public static BufferedImage getList() {
		ImagePrintStream ips=new ImagePrintStream();
		int i=0;
		for(CreditTrade ct:CreditTrade.trades) {
			ips.append(++i).append("、").println(ct.toString());
		}
		ips.println("输入@我 购买 序号 来购买物品");
		return ips.asImage();
	}
}