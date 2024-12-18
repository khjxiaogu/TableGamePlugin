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
package com.khjxiaogu.TableGames.platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import org.apache.http.util.Args;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.khjxiaogu.TableGames.data.application.BindingDatabase;
import com.khjxiaogu.TableGames.data.application.CreditTrade;
import com.khjxiaogu.TableGames.data.application.GenericPlayerData;
import com.khjxiaogu.TableGames.data.application.PlayerCreditData;
import com.khjxiaogu.TableGames.data.application.PlayerDatabase;
import com.khjxiaogu.TableGames.game.clue.ClueGame;
import com.khjxiaogu.TableGames.game.clue.CluePreserve;
import com.khjxiaogu.TableGames.game.fastclue.FastClueGame;
import com.khjxiaogu.TableGames.game.fastclue.FastCluePreserve;
import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomSolitare;
import com.khjxiaogu.TableGames.game.idiomsolitare.SolitarePreserve;
import com.khjxiaogu.TableGames.game.kalah.ComputerKalahBoard;
import com.khjxiaogu.TableGames.game.kalah.PlayerBoard;
import com.khjxiaogu.TableGames.game.spwarframe.SpWarframe;
import com.khjxiaogu.TableGames.game.spwarframe.SpWarframePreserve;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverGame;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverHolder;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverHolderPreserve;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverPreserve;
import com.khjxiaogu.TableGames.game.werewolf.GodWerewolfPreserve;
import com.khjxiaogu.TableGames.game.werewolf.GodWerewolfCreater;
import com.khjxiaogu.TableGames.game.werewolf.StandardWerewolfCreater;
import com.khjxiaogu.TableGames.game.werewolf.StandardWerewolfPreserve;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.Role;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPlayerData;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPreserve;
import com.khjxiaogu.TableGames.permission.GlobalMatcher;
import com.khjxiaogu.TableGames.permission.MatchInfo;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.FileUtil;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.KExecutor;
import com.khjxiaogu.TableGames.utils.PreserveHolder;
import com.khjxiaogu.TableGames.utils.PreserveInfo;
import com.khjxiaogu.TableGames.utils.TimeUtil;
import com.khjxiaogu.TableGames.utils.Utils;

public class GlobalMain {
	public static PlayerDatabase db;
	public static PlayerCreditData credit;
	public static BindingDatabase bindings;
	private static UnifiedLogger logger;
	public static GlobalMatcher privmatcher = new GlobalMatcher();
	public static File dataFolder;
	private static boolean hasInited;
	public static final LinkedList<Consumer<Long>> expireTasks=new LinkedList<>();
	public static UnifiedLogger getLogger() {
		return logger;
	}

	public static void init(File dataFolder) {
		if (hasInited)
			return;
		hasInited = true;
		GlobalMain.dataFolder = dataFolder;
		GlobalMain.bindings = new BindingDatabase(dataFolder);
		GlobalMain.db = new PlayerDatabase(dataFolder);
		GlobalMain.credit = new PlayerCreditData(dataFolder);
		UserIdentifierSerializer.addRawSerializer(e -> QQId.of(Long.parseLong(e)));
		UserIdentifierSerializer.addRawSerializer(e -> SBId.load(e));
		MarkovHelper.loadConfig(dataFolder);
		new Thread(()->{
			while(true) {
				long time=TimeUtil.getTime();
				for(Consumer<Long> task:expireTasks) {
					try {
						task.accept(time);
					}catch(Exception ex) {
						logger.error(ex);
						logger.warning("Exception during executing expire task");
					}
				}
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					return;
				}
			}
		}).start();
	}

	public static void setLogger(UnifiedLogger logger) {
		GlobalMain.logger = logger;
	}

	public static Map<String, BiConsumer<RoomMessageEvent, String[]>> normcmd = new ConcurrentHashMap<>();
	public static Map<String, String> normhelp = new LinkedHashMap<>();
	public static Map<String, String> privhelp = new LinkedHashMap<>();
	public static Map<String, BiConsumer<RoomMessageEvent, String[]>> pvmgcmd = new ConcurrentHashMap<>();
	public static Map<String, BiConsumer<RoomMessageEvent, String[]>> privcmd = new ConcurrentHashMap<>();
	public static ExecutorService dispatchexec = Executors.newCachedThreadPool();
	public static List<String> gameList = new ArrayList<>();

	public static void defaultFirePrivate(String command, UserIdentifier senderId, IMessageCompound msg,
			Supplier<RoomMessageEvent> ev) {
		if (command.startsWith("##"))
			GlobalMain.firePrivateCommand(Utils.removeLeadings("##", command), ev);
		DynamicListeners.dispatchAsync(senderId, MsgType.PRIVATE, msg);
	}

	public static void firePrivateCommand(String command, Supplier<RoomMessageEvent> ev) {
		String[] args = command.split(" ");
		BiConsumer<RoomMessageEvent, String[]> bae = GlobalMain.pvmgcmd.get(args[0]);
		if (bae != null)
			bae.accept(ev.get(), args);
	}

	public static void firePublicCommand(String command, UserIdentifier uid, Supplier<AbstractUser> user,
			Supplier<RoomMessageEvent> rev, UserIdentifier rid, Permission perm, UserIdentifier botid,
			IMessageCompound msg) {
		if (command != null) {
			DynamicListeners.dispatch(uid, rid, user, MsgType.AT, msg);
			String[] args = command.split(" ");

			BiConsumer<RoomMessageEvent, String[]> bae = GlobalMain.normcmd.get(args[0]);
			BiConsumer<RoomMessageEvent, String[]> bce = GlobalMain.privcmd.get(args[0]);
			if (bae == null && bce == null)
				return;
			MatchInfo miadm = new MatchInfo("admin", uid, rid, perm, botid);
			miadm.mustMatchCommand();
			MatchInfo mi = new MatchInfo(args[0], uid, rid, perm, botid);
			//mi.mustMatchCommand();
			if (bae != null && GlobalMain.privmatcher.match(mi).isAllowed())
				bae.accept(rev.get(), args);
			else if (bce != null && (GlobalMain.privmatcher.match(miadm).isForceAllowed()||GlobalMain.privmatcher.match(mi).isForceAllowed())) 
				bce.accept(rev.get(), args);
			
		} else
			DynamicListeners.dispatchAsync(uid, rid, user, MsgType.PUBLIC, msg);
	}

	public static void addCmd(String cmd, String help, BiConsumer<RoomMessageEvent, String[]> ls) {
		normcmd.put(cmd, ls);
		normhelp.put(cmd, help);
	}

	public static void addPCmd(String cmd, String help, BiConsumer<RoomMessageEvent, String[]> ls) {
		privcmd.put(cmd, ls);
		privhelp.put(cmd, help);
	}

	public static <T extends Game> void makeGame(String name, Class<? extends PreserveInfo<T>> preserver,
			GameCreater<T> gameClass) {
		gameList.add(name);

		normcmd.put("预定" + name, (event, command) -> {
			long ban = GlobalMain.credit.get(event.getSender().getId()).isBanned();
			if (ban == 0)
				PreserveHolder.getPreserve(event.getRoom(), preserver).addPreserver(event.getSender());
			else
				event.getRoom().sendMessage("您已被禁赛直到" + new Date(ban).toString());
		});

		normcmd.put(name + "预定列表", (event, command) -> {
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(), preserver).getPreserveList());
		});
		normcmd.put(name + "统计", (event, command) -> {
			if (command.length == 1) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage()
						.append(db.getPlayer(event.getSender().getId(), name).toString()));
			} else {
				UserIdentifier id = UserIdentifierSerializer.read(command[1]);
				event.getRoom().sendMessage(
						event.getRoom().get(id).getNameCard() + "的" + GlobalMain.db.getPlayer(id, name).toString());
			}
		});
		normcmd.put(name + "分析", (event, command) -> {
			if (command.length == 2) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage()
						.append(db.getPlayer(event.getSender().getId(), name).getStatistic(command[1])));
			} else if (command.length == 3) {
				UserIdentifier id = UserIdentifierSerializer.read(command[2]);
				event.getRoom().sendMessage(event.getRoom().get(id).getNameCard() + "的"
						+ GlobalMain.db.getPlayer(id, name).getStatistic(command[1]));
			}
		});
		normcmd.put("取消预定" + name, (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).removePreserver(event.getSender());
		});
		normcmd.put("查询" + name + "参数", (event, command) -> {
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(), preserver).getArgs());
		});
		privcmd.put("设置" + name + "参数", (event, command) -> {
			String[] args = Arrays.copyOfRange(command, 1, command.length);
			PreserveHolder.getPreserve(event.getRoom(), preserver).setArgs(args);
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(), preserver).getArgs());
			event.getRoom().sendMessage("特殊场已经设置，欢迎发送“##预定" + name + "”参与。");
		});
		privcmd.put("清除" + name + "参数", (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).clearArgs();
			event.getRoom().sendMessage(name + "已经重置为普通场。");
		});
		privcmd.put("立即开始" + name, (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).startNow();
		});
		privcmd.put("强制开始" + name, (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).startForce();
		});
		privcmd.put("清空" + name + "预定", (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).removeAll();
		});
		privcmd.put(name + "提醒", (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver).notifyPreserver();
			event.getRoom().sendMessage("已经提醒所有预定玩家");
		});

		privcmd.put("强制预定" + name, (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver)
					.addPreserver(event.getRoom().get(UserIdentifierSerializer.read(command[1])));
		});
		privcmd.put("强制取消预定" + name, (event, command) -> {
			PreserveHolder.getPreserve(event.getRoom(), preserver)
					.removePreserver(event.getRoom().get(UserIdentifierSerializer.read(command[1])), true);
		});
		/*
		 * privcmd.put(name+"统计", (event,command)->{
		 * event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(db.
		 * getGame(name).getPlayer(event.getSender().getId(),PlayerDatabase.datacls.get(
		 * name)).toString()));
		 * });
		 */
		privcmd.put("b" + name, (event, command) -> {
			PreserveInfo<?> pi = PreserveHolder.getPreserve(event.getRoom(), preserver);
			pi.enablefake = !pi.enablefake;
		});
		privcmd.put(name + "全局统计", (event, command) -> {
			GenericPlayerData<? extends GenericPlayerData<?>>[] ds = GlobalMain.db.getPlayers(name);
			for (int i = 1; i < ds.length; i++) {
				ds[0].plusa(ds[i]);
			}
			event.getRoom().sendMessage("全局" + ds[0].toString());
		});
		privcmd.put(name + "全局分析", (event, command) -> {
			GenericPlayerData<? extends GenericPlayerData<?>>[] ds = GlobalMain.db.getPlayers(name);
			for (int i = 1; i < ds.length; i++) {
				ds[0].plusa(ds[i]);
			}
			if (command.length > 1)
				event.getRoom().sendMessage("全局" + ds[0].getStatistic(command[1]));
			else
				event.getRoom().sendMessage("全局" + ds[0].getStatistic("全部"));
		});
		privcmd.put("开始" + name, (event, command) -> {
			GameUtils.createGame(gameClass, event.getRoom(), Integer.parseInt(command[1]));
			event.getRoom().sendMessage(name + "游戏已经创建，请发送“##报名” 来报名。");
		});
		privcmd.put("定制" + name, (event, command) -> {
			GameUtils.createGame(gameClass, event.getRoom(), Arrays.copyOfRange(command, 1, command.length));
			event.getRoom().sendMessage(name + "游戏已经创建，请发送“##报名”来报名。");
		});

		pvmgcmd.put(name, (event, command) -> {

			class Ptr {
				boolean success = false;
			}
			Ptr t = new Ptr();
			PreserveHolder.getPreserves(event.getSender().getId(), preserver).forEach(p -> {
				t.success |= p.addConfig(event.getSender().getId(), command[1], command[2]);
				getLogger().debug("setting");
			});
			if (t.success)
				event.getSender().sendPrivate("设置成功！");
			else
				event.getSender().sendPrivate("设置失败！");
		});

	}

	public static class BindingTicket {
		public UserIdentifier nid;
		public String token;

		public BindingTicket(UserIdentifier nid, String token) {
			super();
			this.nid = nid;
			this.token = token;
		}
	}

	static {
		normhelp.put("预定<游戏名>", "参加游戏");
		normhelp.put("取消预定<游戏名>", "退出游戏");
		normhelp.put("<游戏名>预定列表", "查看游戏参加名单");
		normhelp.put("<游戏名>统计", "查看游戏统计");
		normhelp.put("<游戏名>统计 [用户ID]", "查看他人游戏统计");
		normhelp.put("<游戏名>", "<指令> <参数>游戏特殊指令");
		privhelp.put("设置<游戏名>参数", "<参数>设置游戏参数");
		privhelp.put("清除<游戏名>参数", "清除游戏参数");
		privhelp.put("强制开始<游戏名>", "强行立即开始游戏");
		privhelp.put("立即开始<游戏名>", "尽快开始游戏");
		privhelp.put("清空<游戏名>预定", "清空预定列表");
		privhelp.put("<游戏名>提醒", "提醒玩家开始游戏");
		privhelp.put("强制预定<游戏名>", "<用户ID>强制玩家参加游戏");
		privhelp.put("强制取消预定<游戏名>", "<用户ID>强制玩家退出游戏");
		privhelp.put("开始<游戏名>", "<人数>开始固定场");
		privhelp.put("定制<游戏名>", "<参数>开始设置场");
		addCmd("组字", "使用KAGE生成字符", (event, args) -> {
			try {
				JsonObject jo = JsonParser
						.parseString(FileUtil.readString(FileUtil
								.fetch("https://zi.tools/api/ids/lookupids/" + URLEncoder.encode(args[1], "UTF-8"))))
						.getAsJsonObject();

				JsonElement je = jo.get(args[1]);
				if (je.isJsonNull())
					event.getSender().sendPublic("Invalid!");
				else {
					JsonObject ch = je.getAsJsonObject();
					String ret = null;
					if (ch.has("kage")) {
						ret = ch.get("kage").getAsString();
					} else if (ch.has("lv1")) {
						String rch = ch.get("lv1").getAsJsonObject().get("match_u_list").getAsJsonArray().get(0)
								.getAsString();
						if (jo.has("font")) {
							JsonObject font = jo.get("font").getAsJsonObject();
							if (font.has(rch)) {
								File temp = new File(dataFolder, "temp");
								temp.mkdirs();
								String id = String.valueOf(TimeUtil.getTime());
								File out = new File(temp, id + ".svg");
								File in = new File(temp, id + ".jpg");
								String svg = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
										+ "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">";

								svg += "<svg width=\"200\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\"><path d=\""
										+ font.get(rch).getAsString() + "\"></path></svg>";
								FileUtil.transfer(svg, out);
								Process p = Runtime.getRuntime()
										.exec("magick -density 200 \"" + out.getAbsolutePath()
												+ "\" -fill white -opaque none -colorspace RGB -resize 200x200 \""
												+ in.getAbsolutePath() + "\"");
								FileUtil.transfer(p.getInputStream(), System.out);
								FileUtil.transfer(p.getErrorStream(), System.err);
								try {
									p.waitFor();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								out.delete();
								event.getSender().sendPublic(
										new Image(ImageIO.read(in)).asMessage().append("\nUnicode: " + rch).append(
												"\nCode:" + Integer.toHexString(rch.codePointAt(0)).toUpperCase()));
								in.delete();
								return;

							}

						}
						event.getSender()
								.sendPublic(Utils.sendTextAsImageUnicode(rch, event.getRoom()).asMessage()
										.append("\nUnicode: " + rch)
										.append("\nCode:" + Integer.toHexString(rch.codePointAt(0)).toUpperCase()));

						return;
					}
					if (ret != null)
						event.getSender()
								.sendPublic(new Image(FileUtil
										.readAll(FileUtil.fetch("https://glyphwiki.org/get_preview_glyph.cgi?data="
												+ URLEncoder.encode(ret, "UTF-8")))));
					else
						event.getSender().sendPublic("Invalid!");
				}
			} catch (JsonSyntaxException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				event.getRoom().sendMessage("Internal Error");
			}

		});
		addPCmd("揭示", "显示游戏的系统信息", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				g.forceShow(event.getSender());
			}
		});
		addPCmd("狼人杀胜率迭代", "迭代狼人杀胜率", (event, command) -> {
			LinkedList<Map.Entry<UserIdentifier, WerewolfPlayerData>> ds = new LinkedList<>(
					GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).entrySet());
			ds.removeIf(v -> v.getValue().total <= 120);

			for (Map.Entry<UserIdentifier, WerewolfPlayerData> ent:ds) {
				ent.getValue().modifier(0.5f);
				GlobalMain.db.getGame("狼人杀").setPlayer(ent.getKey(), ent.getValue());
			}
			event.getRoom().sendMessage("折算胜率完成。");
		});
		addPCmd("狼人杀胜率合并", "把1合并到2的狼人杀胜率", (event, command) -> {
			UserIdentifier from=UserIdentifier.parseUserIdentifier(command[1]);
			UserIdentifier to=UserIdentifier.parseUserIdentifier(command[2]);
			
			WerewolfPlayerData fromcls= GlobalMain.db.getGame("狼人杀").getPlayerRaw(from, WerewolfPlayerData.class);
			WerewolfPlayerData tocls= GlobalMain.db.getGame("狼人杀").getPlayerRaw(to, WerewolfPlayerData.class);
			tocls.forcePlus(fromcls);
			GlobalMain.db.getGame("狼人杀").setPlayerRaw(to,tocls );
			GlobalMain.db.getGame("狼人杀").setPlayerRaw(from,new WerewolfPlayerData() );
			event.getRoom().sendMessage("折算胜率完成。");
		});
		addCmd("狼人杀胜率排行", "查看狼人杀胜率排名", (event, command) -> {
			LinkedList<Map.Entry<UserIdentifier, WerewolfPlayerData>> ds = new LinkedList<>(
					GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).entrySet());
			ds.removeIf(v -> v.getValue().total <= 40);

			ds.sort(Comparator.comparingDouble(v -> v.getValue().wins * 1d / (v.getValue().alive+v.getValue().death)));
			StringBuilder sb = new StringBuilder("胜率前十：");
			for (int i = 0; i < 10; i++) {
				Map.Entry<UserIdentifier, WerewolfPlayerData> ent = ds.pollLast();
				if (ent != null) {
					AbstractUser au = event.getRoom().get(ent.getKey());
					if (au == null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()).append(" ")
							.append(Utils.percent(ent.getValue().wins, ent.getValue().alive+ent.getValue().death)).append("/共")
							.append(ent.getValue().total);
				} else
					break;

			}
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀存活率排行", "查看狼人杀存活率排名", (event, command) -> {
			LinkedList<Map.Entry<UserIdentifier, WerewolfPlayerData>> ds = new LinkedList<>(
					GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).entrySet());
			ds.removeIf(v -> v.getValue().total <= 40);

			ds.sort(Comparator.comparingDouble(v -> v.getValue().alive * 1d / (v.getValue().alive+v.getValue().death)));
			StringBuilder sb = new StringBuilder("存活率前十：");
			for (int i = 0; i < 10; i++) {
				Map.Entry<UserIdentifier, WerewolfPlayerData> ent = ds.pollLast();
				if (ent != null) {
					AbstractUser au = event.getRoom().get(ent.getKey());
					if (au == null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()).append(" ")
							.append(Utils.percent(ent.getValue().alive, ent.getValue().alive+ent.getValue().death)).append("/共")
							.append(ent.getValue().total);
				} else
					break;

			}
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀存活率倒数", "查看狼人杀存活率倒数", (event, command) -> {
			LinkedList<Map.Entry<UserIdentifier, WerewolfPlayerData>> ds = new LinkedList<>(
					GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).entrySet());
			ds.removeIf(v -> v.getValue().total <= 40);

			ds.sort(Comparator.comparingDouble(v -> v.getValue().alive * 1d / (v.getValue().alive+v.getValue().death)));
			StringBuilder sb = new StringBuilder("存活率倒十：");
			for (int i = 0; i < 10; i++) {
				Map.Entry<UserIdentifier, WerewolfPlayerData> ent = ds.poll();
				if (ent != null) {
					AbstractUser au = event.getRoom().get(ent.getKey());
					if (au == null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()).append(" ")
							.append(Utils.percent(ent.getValue().alive, ent.getValue().alive+ent.getValue().death)).append("/共")
							.append(ent.getValue().total);
				} else
					break;

			}
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀胜率倒数", "查看狼人杀胜率倒数", (event, command) -> {
			LinkedList<Map.Entry<UserIdentifier, WerewolfPlayerData>> ds = new LinkedList<>(
					GlobalMain.db.getDatas("狼人杀", WerewolfPlayerData.class).entrySet());
			ds.removeIf(v -> v.getValue().total <= 40);

			ds.sort(Comparator.comparingDouble(v -> v.getValue().wins * 1d / (v.getValue().alive+v.getValue().death)));
			StringBuilder sb = new StringBuilder("胜率倒十：");
			for (int i = 0; i < 10; i++) {
				Map.Entry<UserIdentifier, WerewolfPlayerData> ent = ds.poll();
				if (ent != null) {
					AbstractUser au = event.getRoom().get(ent.getKey());
					if (au == null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()).append(" ")
							.append(Utils.percent(ent.getValue().wins,ent.getValue().alive+ent.getValue().death)).append("/共")
							.append(ent.getValue().total);
				} else
					break;

			}
			event.getRoom().sendMessage(sb.toString());
		});
		addPCmd("权限", "设置权限", (event, args) -> {
			try {
				privmatcher.loadString(args[1]);
				event.getSender().sendPrivate("权限设置成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限设置失败！");
				// getLogger().warning(ex);
			}
		});
		addPCmd("重载权限", "重载权限系统", (event, args) -> {
			try {
				privmatcher.reload();
				event.getSender().sendPrivate("权限重载成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限重载失败！");
				// getLogger().warning(ex);
			}
		});
		addPCmd("设置权限", "重配权限系统", (event, args) -> {
			try {
				privmatcher.rebuildConfig();
				event.getSender().sendPrivate("权限设置成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限设置失败！");
				// getLogger().warning(ex);
			}
		});
		addPCmd("执行", "以他人身份执行", (event, args) -> {
			try {
				DynamicListeners.dispatchAsync(UserIdentifierSerializer.read(args[1]), MsgType.valueOf(args[2]),
						new Text(args[3]).asMessage());
				event.getSender().sendPrivate("成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("失败！");
				// getLogger().warning(ex);
			}
		});
		privcmd.put("enrb", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.ergroup.add(ar.getId().serialize());
			ar.sendMessage("马氏回声已开启");

		});
		privcmd.put("derb", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.ergroup.remove(ar.getId().serialize());
			ar.sendMessage("已静默");

		});
		privcmd.put("rbescape", (event, args) -> {
			MarkovHelper.ignores.add(args[1]);
			event.getRoom().sendMessage("已忽视 "+args[1]);

		});
		privcmd.put("rbunescape", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.ignores.remove(args[1]);
			ar.sendMessage("已恢复访问 "+args[1]);
		});
		privcmd.put("rbterminate", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.nlgroup.add(ar.getId().serialize());
			event.getRoom().sendMessage("已忽视 ");

		});
		privcmd.put("rbrestore", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.nlgroup.remove(ar.getId().serialize());
			ar.sendMessage("已恢复访问 ");
		});
		privcmd.put("rbrestore", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			MarkovHelper.nlgroup.remove(ar.getId().serialize());
			ar.sendMessage("已恢复访问 ");
		});
		privcmd.put("rbchance", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			if(args.length>1) {
				try {
				MarkovHelper.prob.put(ar.getId().serialize(),String.valueOf(Float.parseFloat(args[1])));
				ar.sendMessage("修改概率成功！");
				}catch(NumberFormatException e) {
					ar.sendMessage("失败，非法数值。");
				}
			}else {
				MarkovHelper.prob.remove(ar.getId().serialize());
				ar.sendMessage("设置为默认数值！");
			}
			
		});
		privcmd.put("rbprofile", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			if(args.length>1) {
				MarkovHelper.prof.put(ar.getId().serialize(),args[1]);
				ar.sendMessage("修改模型成功！");
			}else {
				MarkovHelper.prof.remove(ar.getId().serialize());
				ar.sendMessage("设置为默认模型！");
			}
			
		});
		privcmd.put("rbappmode", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			if(args.length>1) {
				MarkovHelper.getMarkov(ar.getId().serialize()).applicationMode=args[1].equals("true");
				ar.sendMessage("设置模式成功！");
			}else {
				if(MarkovHelper.getMarkov(ar.getId().serialize()).applicationMode)
					ar.sendMessage("工作模式");
				else
					ar.sendMessage("测试模式");
			}
			
		});
		privcmd.put("rbmaxlen", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			if(args.length>1) {
				MarkovHelper.getMarkov(ar.getId().serialize()).maxLen=Integer.parseInt(args[1]);
				ar.sendMessage("设置长度成功！");
			}else {
				MarkovHelper.getMarkov(ar.getId().serialize()).maxLen=100;
				ar.sendMessage("恢复默认长度！");
			}
			
		});
		privcmd.put("rbprofiles", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			ar.sendMessage(String.join(",",MarkovHelper.profiles()));
		});
		privcmd.put("rbreadonly", (event, args) -> {
			AbstractRoom ar = event.getRoom();
			if(args.length>1) {
				MarkovHelper.getMarkov(ar.getId().serialize()).readOnly=args[1].equals("true");
				ar.sendMessage("修改模型成功！");
			}else {
				if(MarkovHelper.getMarkov(ar.getId().serialize()).readOnly)
					ar.sendMessage("模型只读");
				else
					ar.sendMessage("模型可写");
			}
		});
		addPCmd("rbhelp", "查看马氏回声帮助", (event, args) -> {
			StringBuilder sb=new StringBuilder("rbterminate 忽视本群\r\n")
					.append("rbrestore 取消忽视本群\r\n")
					.append("rbescape <账号>忽视\r\n")
					.append("rbunescape <账号>取消忽视\r\n")
					.append("enrb 打开\r\n")
					.append("derb 关闭\r\n")
					.append("rbchance [prob] 设置概率\r\n")
					.append("rbprofile [profname] 设置模型数据库\r\n")
					.append("rbprofiles 查看模型数据库列表\r\n")
					.append("rbreadonly [state]查看或者设置模型的只读状态\r\n")
					.append("#rb<header>3> 要求改写对应信息\r\n")
					.append("#srb<seed> <header>3>以固定种子生成信息\r\n")
					.append("#gnr[seed]以固定种子或者随机种子直接生成一段文本\r\n")
					.append("#rbappmode [mode]设置工作模式\r\n")
					.append("#rbmaxlen [len]设置长度限制\r\n")
					.append("https://www.khjxiaogu.com/rainbowapis.html 查看在线api列表");
			event.getRoom().sendMessage(sb.toString());
		});
		addPCmd("测试权限", "测试成员权限", (event, args) -> {
			try {
				
				UserIdentifier uid=UserIdentifier.parseUserIdentifier(args[1]);
				AbstractUser user=event.getRoom().get(uid);
				MatchInfo mi=new MatchInfo(args[2],uid,event.getRoom().getId(),user!=null?user.getPermission():Permission.USER,event.getSender().getHostId());
				if(args.length>3)
					mi.mustMatchCommand();
				event.getRoom().sendMessage(args[1] + "的权限状态为:"
						+ privmatcher.match(mi).name());
			} catch (Exception ex) {
				// getLogger().warning(ex);
			}
		});
		normcmd.put("查任务",(event,args)->{
			event.getRoom().sendMessage(TMWK.checkWiki("TWR任务： "+args[1]));
		});
		normcmd.put("查研究",(event,args)->{
			event.getRoom().sendMessage(TMWK.checkWiki("TWR研究： "+args[1]));
		});
		addPCmd("跳过", "跳过当前等待", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				g.forceSkip();
			}
		});
		addPCmd("接管", "<游戏号码> <账号>用账号接管游戏成员", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				long m1 = Long.parseLong(args[1]);
				AbstractUser m2 = null;
				if (args.length > 2) {
					m2 = event.getRoom().get(UserIdentifierSerializer.read(args[2]));
				}

				if (g.takeOverMember(m1, m2)) {
					event.getRoom().sendMessage("接管成功");
				} else {
					event.getRoom().sendMessage("接管失败");
				}
			}
		});
		privcmd.put("CMD", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null) {
				g.specialCommand(event.getSender(), Arrays.copyOfRange(args, 1, args.length));
			}
		});
		privcmd.put("TTI", (event, args) -> {
			event.getRoom().sendMessage(new Image(Utils.textAsImage(String.join(" ", args))));
		});

		addCmd("绑定", "绑定其它平台账号", (event, args) -> {
			String id = UUID.randomUUID().toString();
			bindings.putTicket(UserIdentifierSerializer.read(args[1]),
					new BindingTicket(event.getSender().getId(), id));

			event.getRoom()
					.sendMessage(event.getSender().getAt().asMessage().append("请用您需要绑定的账号在机器人所在群聊发送\n##确认绑定 " + id));

		});
		addCmd("确认绑定", "绑定其它平台账号", (event, args) -> {
			UserIdentifier id = event.getSender().getId();
			BindingTicket bt = bindings.getTicket(id);
			if (bt.token.equals(args[1])) {
				bindings.putBinding(bt.nid, id);
				event.getRoom().sendMessage("绑定成功！");
				bindings.delTicket(id);
			} else
				event.getRoom().sendMessage("绑定失败，请重新请求绑定！");

		});
		addCmd("查询积分", "查询积分和物品", (event, args) -> {
			event.getRoom().sendMessage(
					event.getSender().getAt().asMessage().append(credit.get(event.getSender().getId()).toString()));
		});
		addCmd("积分商城", "查看积分商城", (event, args) -> {
			event.getRoom().sendMessage(CreditTrade.getList());
		});
		addCmd("购买", "<序号>购买物品", (event, args) -> {
			int is = Integer.parseInt(args[1]);
			if (is > CreditTrade.trades.size() + 1 || is < 1) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("非法商品序号"));
				return;
			}
			if (CreditTrade.trades.get(is - 1).execute(event.getSender().getId())) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("购买成功"));
			} else {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("购买失败"));
			}
		});
		addPCmd("给积分", "<账号> <积分>给予玩家积分", (event, args) -> {
			double crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1]))
					.givePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("添加成功，现有" + crp + "积分");
		});
		addPCmd("扣积分", "<账号> <积分>扣除玩家积分", (event, args) -> {
			double crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1]))
					.removePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("扣除成功，还剩" + crp + "积分");
		});
		addPCmd("禁赛", "<账号> <小时>禁赛玩家", (event, args) -> {
			GlobalMain.credit.get(UserIdentifierSerializer.read(args[1]))
					.addBan(1000L * 3600L * Long.parseLong(args[2]));
			event.getRoom().sendMessage("已经禁赛到"
					+ new Date(GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).isBanned()).toString());
		});
		privcmd.put("解除禁赛114514", (event, args) -> {
			GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).removeBan();
			event.getRoom().sendMessage("已经解除！");
		});
		addPCmd("使用积分", "<账号> <积分>减少玩家积分", (event, args) -> {
			double crp;
			if ((crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1]))
					.withdrawPT(Integer.parseInt(args[2]))) < 0) {
				event.getRoom().sendMessage("扣除失败，积分还差" + -crp + "点");
			}
			event.getRoom().sendMessage("扣除成功，还剩" + crp + "积分");
		});
		addPCmd("给物品", "<账号> <物品名> [数量]给玩家物品", (event, args) -> {
			int cnt = args.length > 3 ? Integer.parseInt(args[3]) : 1;
			int crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).giveItem(args[2], cnt);
			event.getRoom().sendMessage("添加成功，现有" + crp + "个" + args[2]);
		});
		addPCmd("扣物品", "<账号> <物品名> [数量]扣除玩家物品", (event, args) -> {
			int cnt = args.length > 3 ? Integer.parseInt(args[3]) : 1;
			int crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).removeItem(args[2], cnt);
			event.getRoom().sendMessage("扣除成功，还剩" + crp + "个" + args[2]);
		});
		addPCmd("使用物品", "<账号> <物品名> [数量]减少玩家物品", (event, args) -> {
			int crp;
			int cnt = args.length > 3 ? Integer.parseInt(args[3]) : 1;
			if ((crp = GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).withdrawItem(args[2], cnt)) < 0) {
				event.getRoom().sendMessage("扣除失败，" + args[2] + "还差" + -crp + "个");
			}
			event.getRoom().sendMessage("扣除成功，还剩" + crp + "个" + args[2]);
		});
		addCmd("游戏列表", "查看游戏列表", (event, args) -> {
			StringBuilder sb = new StringBuilder("可用的游戏：\n");
			sb.append(String.join("，", gameList));
			sb.append("\n欢迎使用“##预定(游戏名)”预定！");
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("?", "查看命令列表", (event, args) -> {
			StringBuilder sb = new StringBuilder("可用的指令：");
			for (Entry<String, String> i : normhelp.entrySet()) {
				sb.append("\n").append(i.getKey()).append(" ").append(i.getValue());
			}
			event.getRoom().sendMessage(sb.toString());
			if (privmatcher.match("?",event.getSender()).isForceAllowed()) {
				for (Entry<String, String> i : privhelp.entrySet()) {
					sb.append("\n").append(i.getKey()).append(" ").append(i.getValue());
				}
				event.getSender().sendPrivate(sb.toString());
			}
		});
		addCmd("报名", "报名参加当前游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				g.addMember(event.getSender());
			}
		});
		addCmd("kalah","[棋洞数目] [初始棋子数目]游玩宝石棋",(event,arg)->{
			int ic=4;
			int ch=6;
			if(arg.length>1)
				ch=Integer.parseInt(arg[1]);
			if(arg.length>2)
				ic=Integer.parseInt(arg[2]);
			KExecutor kex=new KExecutor(1,event.getRoom());
			PlayerBoard pkb=new PlayerBoard(event.getSender(),ch,ic);
			ComputerKalahBoard cb=new ComputerKalahBoard("电脑",event.getRoom(),ch,ic);
			kex.execute(()->{
				pkb.host_game(cb);
			});
		});
		addPCmd("强制开始", "强制开始当前游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				g.forceStart();
			}
		});
		addPCmd("停止游戏", "强制停止当前游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null) {
				g.forceStop();
			}
			event.getRoom().sendMessage("已经停止正在进行的游戏！");
			event.getSender().sendPrivate("已经停止正在进行的游戏！");
		});
		addPCmd("暂停游戏", "强制停止当前游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null) {
				g.forceInterrupt();
			}
			event.getSender().sendPrivate("已经暂停正在进行的游戏！");
		});
		addPCmd("强制报名", "<qq>强制玩家报名当前游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				g.addMember(event.getRoom().get(UserIdentifierSerializer.read(args[1])));
			}
		});
		addPCmd("继续游戏", "继续暂停的游戏", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				event.getRoom().sendMessage("因为有其他的游戏正在运行，无法继续。");
				return;
			}
			try (FileInputStream fileOut = new FileInputStream(new File(dataFolder, event.getRoom() + ".game"));
					ObjectInputStream out = new ObjectInputStream(fileOut)) {
				GameUtils.getGames().put(event.getRoom(), (Game) out.readObject());
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				event.getRoom().sendMessage("继续游戏失败！");
				e.printStackTrace();
			}
		});
		Random ckr = new SecureRandom();
		List<String> cards = new ArrayList<>();
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("平民");
		cards.add("长老");
		cards.add("老流氓");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("狼人");
		cards.add("石像鬼");
		cards.add("白狼王");
		cards.add("白痴");
		cards.add("预言家");
		cards.add("猎人");
		cards.add("女巫");
		cards.add("守卫");
		cards.add("乌鸦");
		cards.add("骑士");
		cards.add("守墓人");
		cards.add("守卫");
		cards.add("猎魔人");
		addPCmd("狼人杀摇号", "模拟狼人杀摇号", (event, args) -> {
			List<Role> larr = WerewolfGame.fairRollRole(Integer.parseInt(args[1]));
			double pts = WerewolfGame.calculateRolePoint(larr);
			StringBuilder sb = new StringBuilder();
			for (Role cls : larr) {
				sb.append(cls.getName()).append(" ");
			}
			sb.append("得分：").append(pts);
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀抽卡", "进行一次虚拟抽卡", (event, args) -> {
			event.getSender().sendPublic(cards.get(ckr.nextInt(cards.size())));
		});
		addCmd("成语接龙", "开始成语接龙", (event, args) -> {
			Game g = GameUtils.getGames().get(event.getRoom());
			if (g != null && g.isAlive()) {
				event.getRoom().sendMessage("因为有其他的游戏正在运行，无法开始。");
				return;
			}
			IdiomSolitare is = GameUtils.createGame(IdiomSolitare::new, event.getRoom(), 1);
			is.startEmpty();
		});
		makeGame("狼人杀", WerewolfPreserve.class, new DefaultGameCreater<>(WerewolfGame.class));
		makeGame("诸神狼人杀", GodWerewolfPreserve.class, new GodWerewolfCreater());
		makeGame("标准狼人杀", StandardWerewolfPreserve.class, new StandardWerewolfCreater());
		makeGame("谁是卧底", UnderCoverPreserve.class, new DefaultGameCreater<>(UnderCoverGame.class));
		makeGame("谁是卧底发词", UnderCoverHolderPreserve.class, new DefaultGameCreater<>(UnderCoverHolder.class));
		makeGame("妙探寻凶", CluePreserve.class, new DefaultGameCreater<>(ClueGame.class));
		makeGame("妙探寻凶X", FastCluePreserve.class, new DefaultGameCreater<>(FastClueGame.class));
		makeGame("SP战纪", SpWarframePreserve.class, new DefaultGameCreater<>(SpWarframe.class));
		makeGame("成语接龙", SolitarePreserve.class, new DefaultGameCreater<>(IdiomSolitare.class));
	}

}
