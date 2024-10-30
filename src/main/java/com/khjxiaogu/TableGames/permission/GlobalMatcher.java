/**
 * Mirai Song Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;

public class GlobalMatcher implements PermissionMatcher {
	BotMatcher global;
	Map<UserIdentifier, BotMatcher> local = new ConcurrentHashMap<>();
	public PermissionResult match(String perm,AbstractUser sender) {
		return match(new MatchInfo(perm,sender.getId(),sender.getRoom().getId(),sender.getPermission(),sender.getHostId()));
	}
	@Override
	public PermissionResult match(MatchInfo info) {
		BotMatcher bm = local.getOrDefault(info.bot.getId(), global);
		return bm.match(info);
	}

	public boolean loadString(String s, UserIdentifier bid) {
		BotMatcher bm = local.computeIfAbsent(bid, x -> new BotMatcher());
		if (bm.loadMatcher(s, null)) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, bid.serialize() + ".permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	public boolean loadString(String s) {
		if (global.loadMatcher(s, null)) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, "global.permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	public void rebuildConfig() {
		try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, "global.permission"), false);
				PrintStream sc = new PrintStream(fis)) {
			boolean nfirst = false;
			for (String s : global.getValue()) {
				if (nfirst)
					sc.println();
				nfirst = true;
				sc.print(s);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for (Entry<UserIdentifier, BotMatcher> i : local.entrySet()) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, i.getKey() + ".permission"), false);
					PrintStream sc = new PrintStream(fis)) {
				boolean nfirst = false;
				for (String s : i.getValue().getValue()) {
					if (nfirst)
						sc.println();
					nfirst = true;
					sc.print(s);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	File loadfrom;
	public void reload() {
		load(loadfrom);
	}
	public void load(File f) {
		loadfrom = f;
		global = null;
		local.clear();
		File gc = new File(f, "global.permission");
		global = new BotMatcher();
		try (FileInputStream fis = new FileInputStream(gc); Scanner sc = new Scanner(fis)) {
			int i = 0;
			while (sc.hasNextLine()) {
				i++;
				try {
					global.loadMatcher(sc.nextLine(), null);
				} catch (Exception ex) {
					GlobalMain.getLogger().error(ex);
					GlobalMain.getLogger().warning("权限配置文件" + gc.getName() + "的第" + i + "行有语法错误！");
				}
			}
		} catch (Exception ex) {
			GlobalMain.getLogger().error(ex);
			GlobalMain.getLogger().warning("权限配置文件" + gc.getName() + "读取失败！" + ex.getMessage());

		}
		for (File ff : f.listFiles()) {
			try {
				if (ff.getName().endsWith(".permission")) {
					String fn = ff.getName().split("\\.")[0];

					UserIdentifier gn = UserIdentifier.parseUserIdentifier(fn);
					if (gn != null) {
						BotMatcher bm = new BotMatcher();
						try (FileInputStream fis = new FileInputStream(ff); Scanner sc = new Scanner(fis)) {
							int i = 0;
							while (sc.hasNextLine()) {
								i++;
								try {
									bm.loadMatcher(sc.nextLine(), null);
									continue;
								} catch (Exception ex) {
									GlobalMain.getLogger().error(ex);
								}
								GlobalMain.getLogger().warning("权限配置文件" + ff.getName() + "的第" + i + "行有语法错误！");
							}
						}
						local.put(gn, bm);
					}
				}
			} catch (Exception ex) {
				GlobalMain.getLogger().error(ex);
				GlobalMain.getLogger().warning("权限配置文件" + ff.getName() + "读取失败：" + ex.getMessage());
			}
		}
	}

	@Override
	public List<String> getValue() {
		return global.getValue();
	}

	public boolean loadString(String s, UserIdentifier gid, UserIdentifier bid) {
		BotMatcher bm = local.computeIfAbsent(bid, x -> new BotMatcher());
		if (bm.loadMatcher(s, gid)) {
			try (FileOutputStream fis = new FileOutputStream(new File(loadfrom, bid + ".permission"), true);
					PrintStream sc = new PrintStream(fis)) {
				sc.println();
				sc.print(s);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return true;
		}
		return false;
	}

	
}
