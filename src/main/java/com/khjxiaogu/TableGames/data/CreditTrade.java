/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.data;

import java.util.ArrayList;

import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;

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
	public boolean execute(UserIdentifier id) {
		PlayerCredit pcd=GlobalMain.credit.get(id);
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
