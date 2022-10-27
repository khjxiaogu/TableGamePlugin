/**
 * Mirai Tablegames Plugin
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
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.game.idiomsolitare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.data.PlayerCredit;
import com.khjxiaogu.TableGames.platform.AbstractRoom;
import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.PreserveLess;
import com.khjxiaogu.TableGames.utils.TimeUtil;
import com.khjxiaogu.TableGames.utils.Utils;

public class IdiomSolitare extends Game implements PreserveLess {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8553092242688868357L;
	Map<AbstractUser, SolitarePlayer> user = new ConcurrentHashMap<>();
	int cplayer;
	Set<IdiomInfo> used = Collections.newSetFromMap(new ConcurrentHashMap<>());
	IdiomInfo current;
	boolean started;
	boolean isAlive;
	boolean isPL;
	long lastsend;
	long endsin;

	public IdiomSolitare(AbstractRoom group, int cplayer) {
		super(group, cplayer, 4);
		this.cplayer = cplayer;
	}

	@Override
	public void forceStart() {
		started = true;
		new Thread(this::gameMain).start();
		;
	}

	@Override
	public String getName() {
		return "成语接龙";
	}

	@Override
	public boolean isAlive() {
		return isAlive;
	}

	@Override
	public boolean onReAttach(UserIdentifier id) {
		return false;
	}

	@Override
	public boolean addMember(AbstractUser mem) {
		if (started)
			return false;
		user.put(mem, new SolitarePlayer(mem));
		if (user.size() >= cplayer) {
			started = true;
			new Thread(this::gameMain).start();
			;
		}
		return true;
	}

	public void gameMain() {
		isAlive=true;
		if (isPL)
			this.sendPublicMessage("成语接龙的玩家都可以在群里使用“我接 成语”的形式进行接龙，接龙次数最多的人获胜！\n接龙词语不能重复！若两分钟以内无人接上，则游戏结束！");
		else
			this.sendPublicMessage("成语接龙的玩家都可以在群里使用“我接 成语”的形式进行接龙，接龙次数最多的人获胜！\n接龙词语不能重复！游戏限时" + 5 * cplayer
					+ "分钟！\n若两分钟以内无人接上，则游戏结束！");
		// int pointspool=cplayer/2;
		if (isPL) {
			this.getGroup().registerRoomListener(this,(u, msg, type) -> {
				if (type == MsgType.PUBLIC) {
					String cont = msg.getText();
					if (cont.startsWith("我接")) {
						cont = Utils.removeLeadings("我接", cont);
						checkSpea(user.computeIfAbsent(u, SolitarePlayer::new), cont);
					}
				}
			});
		} else
			for (SolitarePlayer sp : user.values()) {
				sp.registerListener((msg, type) -> {
					if (type == MsgType.PUBLIC) {
						String cont = msg.getText();
						if (cont.startsWith("我接")) {
							cont = Utils.removeLeadings("我接", cont);
							checkSpea(sp, cont);
						}
					}
				});
			}
		current = IdiomLibrary.random();
		this.sendPublicMessage("第一个成语是：" + current.word);
		lastsend = TimeUtil.getTime() + 120000;
		endsin = 30 * cplayer;
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
			long now = TimeUtil.getTime();
			if (now >= lastsend) {
				break;
			}
			if (!isPL) {
				endsin--;
				if (endsin % 30 == 0) {
					this.sendPublicMessage("游戏时间还剩下" + endsin / 6 + "分钟！");
				}
				if (endsin == 6) {
					this.sendPublicMessage("游戏时间还剩下1分钟！");
				}
				if (endsin == 3) {
					this.sendPublicMessage("游戏时间还剩下30秒！");
				}
				if (endsin == 1) {
					this.sendPublicMessage("游戏时间还剩下10秒！");
				}
				if (endsin <= 0)
					break;
			}
		}
		isAlive=false;
		if(isPL)
			this.getGroup().releaseRoomListener(this);
		this.sendPublicMessage("游戏结束！");
		StringBuilder sb = new StringBuilder("游戏结果：\n");
		double maxpt = 0;
		double minpt = 0;
		int total = 0;
		List<SolitarePlayer> max = new ArrayList<>();
		List<SolitarePlayer> min = new ArrayList<>();
		for (SolitarePlayer sp : user.values()) {
			sp.releaseListener();
			total += sp.pt;
		}
		cplayer = user.size();
		double avg = total * 1.0D / cplayer;
		for (SolitarePlayer sp : user.values()) {
			if (sp.pt < avg) {
				minpt += avg - sp.pt;
				min.add(sp);
			} else {
				maxpt += sp.pt - avg;
				max.add(sp);
			}
		}
		double pt = 0.5 + total / cplayer * 0.01;
		// GlobalMain.getLogger().debug("added pointK"+pt);
		double tpm = 0.25 * cplayer + (maxpt + minpt) / cplayer * 0.1;
		if (total >= 4 * cplayer) {
			for (SolitarePlayer sp : min) {
				PlayerCredit pc = GlobalMain.credit.get(sp.getId());
				double ppm;
				if (minpt > 0)
					ppm = tpm * ((avg - sp.pt) / minpt);
				else
					ppm = tpm / min.size();
				
				if (pc.getPT() >= ppm) {
					if (pc.withdrawPT(PlayerCredit.normalizedb(ppm)) >= 0)
						pt += ppm;
				} else if (pc.getPT() > 0) {
					ppm = pc.getPT();
					if (pc.withdrawPT(ppm) >= 0)
						;
					pt += ppm;
				} else {
					ppm = 0;
				}
				sp.ptchange = -ppm;
			}
			for (SolitarePlayer sp : max) {
				double ppm;
				if (maxpt > 0) {
					ppm = pt * ((sp.pt - avg) / maxpt);
					// GlobalMain.getLogger().debug("added pointA"+maxpt);
				} else {
					ppm = pt / max.size();
					// GlobalMain.getLogger().debug("added pointB"+pt);
				}
				PlayerCredit pc = GlobalMain.credit.get(sp.getId());
				pc.givePT(PlayerCredit.normalizedb(ppm));
				sp.ptchange = ppm;
				// GlobalMain.getLogger().debug("added point"+ppm);
			}
			// GlobalMain.getLogger().debug("added point");
		}

		for (SolitarePlayer sp : user.values()) {
			sb.append(sp.getMemberString() + " " + sp.pt + "个 " + PlayerCredit.normalizedb(sp.ptchange) + "分\n");
		}
		this.sendPublicMessage(sb.toString());
	}

	public synchronized void checkSpea(SolitarePlayer sp, String word) {
		IdiomInfo ii = IdiomLibrary.info.get(word);
		if (ii == null) {
			sp.ptchange-=0.01;
			sp.sendPublic("这个貌似不是成语噢");
			return;
		}
		if (current.endsWith(ii)) {
			if (used.contains(ii)) {
				sp.sendPublic("这个已经接过了！");
				sp.ptchange-=0.01;
				return;
			}
			used.add(ii);
			current = ii;
			sp.pt++;
			lastsend = TimeUtil.getTime() + 120000;
			sp.sendPublic("接龙成功！您当前已接" + sp.pt + "个！\n当前成语：" + current.word);
		}

	}

	@Override
	public void startEmpty() {
		isPL = true;
		started = true;
		new Thread(this::gameMain).start();
	}

	@Override
	public void forceStop() {
		super.forceStop();
		isAlive=false;
		if(isPL)
			this.getGroup().releaseRoomListener(this);
	}
}
