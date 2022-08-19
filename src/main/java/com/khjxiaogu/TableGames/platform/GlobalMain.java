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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.data.CreditTrade;
import com.khjxiaogu.TableGames.data.GenericPlayerData;
import com.khjxiaogu.TableGames.data.PlayerCreditData;
import com.khjxiaogu.TableGames.data.PlayerDatabase;
import com.khjxiaogu.TableGames.game.clue.ClueGame;
import com.khjxiaogu.TableGames.game.clue.CluePreserve;
import com.khjxiaogu.TableGames.game.fastclue.FastClueGame;
import com.khjxiaogu.TableGames.game.fastclue.FastCluePreserve;
import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomSolitare;
import com.khjxiaogu.TableGames.game.idiomsolitare.SolitarePreserve;
import com.khjxiaogu.TableGames.game.spwarframe.SpWarframe;
import com.khjxiaogu.TableGames.game.spwarframe.SpWarframePreserve;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverGame;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverHolder;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverHolderPreserve;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverPreserve;
import com.khjxiaogu.TableGames.game.werewolf.MiniWerewolfPreserve;
import com.khjxiaogu.TableGames.game.werewolf.StandardWerewolfCreater;
import com.khjxiaogu.TableGames.game.werewolf.StandardWerewolfPreserve;
import com.khjxiaogu.TableGames.game.werewolf.Villager;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame.Role;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPlayerData;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPreserve;
import com.khjxiaogu.TableGames.permission.GlobalMatcher;
import com.khjxiaogu.TableGames.platform.message.Image;
import com.khjxiaogu.TableGames.utils.DefaultGameCreater;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameCreater;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.PreserveHolder;
import com.khjxiaogu.TableGames.utils.PreserveInfo;
import com.khjxiaogu.TableGames.utils.Utils;


public class GlobalMain {
	public static PlayerDatabase db;
	public static PlayerCreditData credit;
	private static UnifiedLogger logger;
	public static GlobalMatcher privmatcher=new GlobalMatcher();
	public static File dataFolder;
	@FunctionalInterface
	interface BotCreater{
		AbstractBotUser createBot(int id,Class<? extends BotUserLogic> logicCls,Game in);
	}
	private static BotCreater defaultBotCreater;
	public static UnifiedLogger getLogger() {
		return logger;
	}
	public static void init(File dataFolder) {
		GlobalMain.dataFolder=dataFolder;
		GlobalMain.db=new PlayerDatabase(dataFolder);
		GlobalMain.credit=new PlayerCreditData(dataFolder);
	}
	public static void setLogger(UnifiedLogger logger) {
		GlobalMain.logger=logger;
	}
	public static AbstractBotUser createBot(int id,Class<? extends BotUserLogic> logicCls,Game in) {
		return defaultBotCreater.createBot(id, logicCls, in);
	}
	
	public static void setDefaultBotCreater(BotCreater defaultBotCreater) {
		GlobalMain.defaultBotCreater = defaultBotCreater;
	}
	public static Map<String,BiConsumer<RoomMessageEvent,String[]>> normcmd=new ConcurrentHashMap<>();
	public static Map<String,String> normhelp=new LinkedHashMap<>();
	public static Map<String,String> privhelp=new LinkedHashMap<>();
	public static Map<String,BiConsumer<RoomMessageEvent,String[]>> pvmgcmd=new ConcurrentHashMap<>();
	public static Map<String,BiConsumer<RoomMessageEvent,String[]>> privcmd=new ConcurrentHashMap<>();
	public static ExecutorService dispatchexec=Executors.newCachedThreadPool();
	public static List<String> gameList=new ArrayList<>();
	public static void addCmd(String cmd,String help,BiConsumer<RoomMessageEvent,String[]> ls) {
		normcmd.put(cmd,ls);
		normhelp.put(cmd,help);
	}
	public static void addPCmd(String cmd,String help,BiConsumer<RoomMessageEvent,String[]> ls) {
		privcmd.put(cmd,ls);
		privhelp.put(cmd,help);
	}
	public static <T extends Game> void makeGame(String name,Class<? extends PreserveInfo<T>> preserver,GameCreater<T> gameClass) {
		gameList.add(name);
		
		normcmd.put("预定"+name, (event,command)->{
			long ban=GlobalMain.credit.get(event.getSender().getId()).isBanned();
			if(ban==0)
				PreserveHolder.getPreserve(event.getRoom(),preserver).addPreserver(event.getSender());
			else
				event.getRoom().sendMessage("您已被禁赛直到"+new Date(ban).toString());
		});
		
		normcmd.put(name+"预定列表", (event,command)->{
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(),preserver).getPreserveList());
		});
		normcmd.put(name+"统计", (event,command)->{
			if(command.length==1) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(db.getPlayer(event.getSender().getId(),name).toString()));
			} else {
				UserIdentifier id=UserIdentifierSerializer.read(command[1]);
				event.getRoom().sendMessage(event.getRoom().get(id).getNameCard()+"的"+GlobalMain.db.getPlayer(id,name).toString());
			}
		});
		normcmd.put(name+"分析", (event,command)->{
			if(command.length==2) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(db.getPlayer(event.getSender().getId(),name).getStatistic(command[1])));
			} else if(command.length==3){
				UserIdentifier id=UserIdentifierSerializer.read(command[2]);
				event.getRoom().sendMessage(event.getRoom().get(id).getNameCard()+"的"+GlobalMain.db.getPlayer(id,name).getStatistic(command[1]));
			}
		});
		normcmd.put("取消预定"+name, (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).removePreserver(event.getSender());
		});
		normcmd.put("查询"+name+"参数", (event,command)->{
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(),preserver).getArgs());
		});
		privcmd.put("设置"+name+"参数", (event,command)->{
			String[] args=Arrays.copyOfRange(command,1,command.length);
			PreserveHolder.getPreserve(event.getRoom(),preserver).setArgs(args);
			event.getRoom().sendMessage(PreserveHolder.getPreserve(event.getRoom(),preserver).getArgs());
			event.getRoom().sendMessage("特殊场已经设置，欢迎发送“##预定"+name+"”参与。");
		});
		privcmd.put("清除"+name+"参数", (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).clearArgs();
			event.getRoom().sendMessage(name+"已经重置为普通场。");
		});
		privcmd.put("立即开始"+name, (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).startNow();
		});
		privcmd.put("强制开始"+name, (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).startForce();
		});
		privcmd.put("清空"+name+"预定", (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).removeAll();
		});
		privcmd.put(name+"提醒", (event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).notifyPreserver();
			event.getRoom().sendMessage("已经提醒所有预定玩家");
		});

		privcmd.put("强制预定"+name,(event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).addPreserver(event.getRoom().get(UserIdentifierSerializer.read(command[1])));
		});
		privcmd.put("强制取消预定"+name,(event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).removePreserver(event.getRoom().get(UserIdentifierSerializer.read(command[1])),true);
		});
		/*privcmd.put(name+"统计", (event,command)->{
			event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(db.getGame(name).getPlayer(event.getSender().getId(),PlayerDatabase.datacls.get(name)).toString()));
		});*/
		privcmd.put("b"+name, (event,command)->{
			PreserveInfo<?> pi=PreserveHolder.getPreserve(event.getRoom(),preserver);
			pi.enablefake=!pi.enablefake;
		});
		privcmd.put(name+"全局统计", (event,command)->{
			GenericPlayerData<? extends GenericPlayerData<?>>[] ds=GlobalMain.db.getPlayers(name);
			for(int i=1;i<ds.length;i++) {
				ds[0].plusa(ds[i]);
			}
			event.getRoom().sendMessage("全局"+ds[0].toString());
		});
		privcmd.put(name+"全局分析", (event,command)->{
			GenericPlayerData<? extends GenericPlayerData<?>>[] ds=GlobalMain.db.getPlayers(name);
			for(int i=1;i<ds.length;i++) {
				ds[0].plusa(ds[i]);
			}
			if(command.length>1)
				event.getRoom().sendMessage("全局"+ds[0].getStatistic(command[1]));
			else
				event.getRoom().sendMessage("全局"+ds[0].getStatistic("全部"));
		});
		privcmd.put("开始"+name, (event,command)->{
			GameUtils.createGame(gameClass,event.getRoom(),Integer.parseInt(command[1]));
			event.getRoom().sendMessage(name+"游戏已经创建，请发送“##报名” 来报名。");
		});
		privcmd.put("定制"+name, (event,command)->{
			GameUtils.createGame(gameClass,event.getRoom(),Arrays.copyOfRange(command,1,command.length));
			event.getRoom().sendMessage(name+"游戏已经创建，请发送“##报名”来报名。");
		});
		
		pvmgcmd.put(name,(event,command)->{
			
			class Ptr{
				boolean success=false;
			}
			Ptr t=new Ptr();
			PreserveHolder.getPreserves(event.getSender().getId(),preserver).forEach(p->{
				t.success |=p.addConfig(event.getSender().getId(),command[1],command[2]);
				getLogger().debug("setting");
			});
			if(t.success)
				event.getSender().sendPrivate("设置成功！");
			else
				event.getSender().sendPrivate("设置失败！");
		});

	}

	static {
		normhelp.put("预定<游戏名>","参加游戏");
		normhelp.put("取消预定<游戏名>","退出游戏");
		normhelp.put("<游戏名>预定列表","查看游戏参加名单");
		normhelp.put("<游戏名>统计","查看游戏统计");
		normhelp.put("<游戏名>统计 [用户ID]","查看他人游戏统计");
		normhelp.put("<游戏名>","<指令> <参数>游戏特殊指令");
		privhelp.put("设置<游戏名>参数","<参数>设置游戏参数");
		privhelp.put("清除<游戏名>参数","清除游戏参数");
		privhelp.put("强制开始<游戏名>","强行立即开始游戏");
		privhelp.put("立即开始<游戏名>","尽快开始游戏");
		privhelp.put("清空<游戏名>预定","清空预定列表");
		privhelp.put("<游戏名>提醒","提醒玩家开始游戏");
		privhelp.put("强制预定<游戏名>","<用户ID>强制玩家参加游戏");
		privhelp.put("强制取消预定<游戏名>","<用户ID>强制玩家退出游戏");
		privhelp.put("开始<游戏名>","<人数>开始固定场");
		privhelp.put("定制<游戏名>","<参数>开始设置场");
		addPCmd("揭示","显示游戏的系统信息",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.forceShow(event.getSender());
			}
		});
		addCmd("狼人杀胜率排名","查看狼人杀胜率排名", (event,command)->{
			LinkedList<Map.Entry<UserIdentifier,WerewolfPlayerData>> ds=new LinkedList<>(GlobalMain.db.getDatas("狼人杀",WerewolfPlayerData.class).entrySet());
			ds.removeIf(v->v.getValue().total<10);
			
			ds.sort(Comparator.comparingDouble(v->v.getValue().wins*1d/v.getValue().total));
			StringBuilder sb=new StringBuilder("胜率前十：");
			for(int i=0;i<10;i++) {
				Map.Entry<UserIdentifier,WerewolfPlayerData> ent=ds.pollLast();
				if(ent!=null) {
					AbstractUser au=event.getRoom().get(ent.getKey());
					if(au==null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()+" "+Utils.percent(ent.getValue().wins, ent.getValue().total));
				}else break;
					
			}
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀胜率倒数","查看狼人杀胜率倒数", (event,command)->{
			LinkedList<Map.Entry<UserIdentifier,WerewolfPlayerData>> ds=new LinkedList<>(GlobalMain.db.getDatas("狼人杀",WerewolfPlayerData.class).entrySet());
			ds.removeIf(v->v.getValue().total<10);
			
			ds.sort(Comparator.comparingDouble(v->v.getValue().wins*1d/v.getValue().total));
			StringBuilder sb=new StringBuilder("胜率倒十：");
			for(int i=0;i<10;i++) {
				Map.Entry<UserIdentifier,WerewolfPlayerData> ent=ds.poll();
				if(ent!=null) {
					AbstractUser au=event.getRoom().get(ent.getKey());
					if(au==null) {
						i--;
						continue;
					}
					sb.append("\n").append(au.getNameCard()+" "+Utils.percent(ent.getValue().wins, ent.getValue().total));
				}else break;
					
			}
			event.getRoom().sendMessage(sb.toString());
		});
		addPCmd("权限","设置权限", (event,args)->{
			try {
				privmatcher.loadString(args[1]);
				event.getSender().sendPrivate("权限设置成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限设置失败！");
				//getLogger().warning(ex);
			}
		});
		addPCmd("重载权限","重载权限系统", (event,args)->{
			try {
				privmatcher.reload();
				event.getSender().sendPrivate("权限重载成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限重载失败！");
				//getLogger().warning(ex);
			}
		});
		addPCmd("设置权限","重配权限系统",(event,args)->{
			try {
				privmatcher.rebuildConfig();
				event.getSender().sendPrivate("权限设置成功！");
			} catch (Exception ex) {
				event.getSender().sendPrivate("权限设置失败！");
				//getLogger().warning(ex);
			}
		});
		addPCmd("测试权限", "测试成员权限",(event,args)->{
			try {
				event.getRoom().sendMessage(args[1]+"的权限状态为:"+privmatcher.match(event.getRoom().get(UserIdentifierSerializer.read(args[1]))).name());
			} catch (Exception ex) {
				//getLogger().warning(ex);
			}
		});
		addPCmd("跳过","跳过当前等待",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.forceSkip();
			}
		});
		addPCmd("接管","<游戏号码> <账号>用账号接管游戏成员",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				long m1=Long.parseLong(args[1]);
				AbstractUser m2=null;
				if(args.length>2) {
					m2=event.getRoom().get(UserIdentifierSerializer.read(args[2]));
				}
				
				if(g.takeOverMember(m1,m2)) {
					event.getRoom().sendMessage("接管成功");
				} else {
					event.getRoom().sendMessage("接管失败");
				}
			}
		});
		privcmd.put("CMD",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null) {
				g.specialCommand(event.getSender(),Arrays.copyOfRange(args,1,args.length));
			}
		});
		privcmd.put("TTI",(event,args)->{
			event.getRoom().sendMessage(new Image(Utils.textAsImage(String.join(" ",args))));
		});
		addCmd("查询积分","查询积分和物品",(event,args)->{
			event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(credit.get(event.getSender().getId()).toString()));
		});
		addCmd("积分商城","查看积分商城",(event,args)->{
			event.getRoom().sendMessage(CreditTrade.getList());
		});
		addCmd("购买","<序号>购买物品",(event,args)->{
			int is=Integer.parseInt(args[1]);
			if(is>CreditTrade.trades.size()+1||is<1) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("非法商品序号"));
				return;
			}
			if(CreditTrade.trades.get(is-1).execute(event.getSender().getId())) {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("购买成功"));
			} else {
				event.getRoom().sendMessage(event.getSender().getAt().asMessage().append("购买失败"));
			}
		});
		addPCmd("给积分","<账号> <积分>给予玩家积分",(event,args)->{
			double crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).givePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("添加成功，现有"+crp+"积分");
		});
		addPCmd("扣积分","<账号> <积分>扣除玩家积分",(event,args)->{
			double crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).removePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"积分");
		});
		addPCmd("禁赛","<账号> <小时>禁赛玩家",(event,args)->{
			GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).addBan(1000L*3600L*Long.parseLong(args[2]));
			event.getRoom().sendMessage("已经禁赛到"+new Date(GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).isBanned()).toString());
		});
		privcmd.put("解除禁赛114514",(event,args)->{
			GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).removeBan();
			event.getRoom().sendMessage("已经解除！");
		});
		addPCmd("使用积分","<账号> <积分>减少玩家积分",(event,args)->{
			double crp;
			if((crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).withdrawPT(Integer.parseInt(args[2])))<0) {
				event.getRoom().sendMessage("扣除失败，积分还差"+-crp+"点");
			}
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"积分");
		});
		addPCmd("给物品","<账号> <物品名> [数量]给玩家物品",(event,args)->{
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			int crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).giveItem(args[2],cnt);
			event.getRoom().sendMessage("添加成功，现有"+crp+"个"+args[2]);
		});
		addPCmd("扣物品","<账号> <物品名> [数量]扣除玩家物品",(event,args)->{
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			int crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).removeItem(args[2],cnt);
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"个"+args[2]);
		});
		addPCmd("使用物品","<账号> <物品名> [数量]减少玩家物品",(event,args)->{
			int crp;
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			if((crp=GlobalMain.credit.get(UserIdentifierSerializer.read(args[1])).withdrawItem(args[2],cnt))<0) {
				event.getRoom().sendMessage("扣除失败，"+args[2]+"还差"+-crp+"个");
			}
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"个"+args[2]);
		});
		addCmd("游戏列表","查看游戏列表" ,(event,args)->{
			StringBuilder sb=new StringBuilder("可用的游戏：\n");
			sb.append(String.join("，",gameList));
			sb.append("\n欢迎使用“##预定(游戏名)”预定！");
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("?","查看命令列表",(event,args)->{
			StringBuilder sb=new StringBuilder("可用的指令：");
			for(Entry<String, String> i:normhelp.entrySet()) {
				sb.append("\n").append(i.getKey()).append(" ").append(i.getValue());
			}
			event.getRoom().sendMessage(sb.toString());
			if(privmatcher.match(event.getSender()).isAllowed()) {
				for(Entry<String, String> i:privhelp.entrySet()) {
					sb.append("\n").append(i.getKey()).append(" ").append(i.getValue());
				}
				event.getSender().sendPrivate(sb.toString());
			}
		});
		addCmd("报名","报名参加当前游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.addMember(event.getSender());
			}
		});
		addPCmd("强制开始","强制开始当前游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.forceStart();
			}
		});
		addPCmd("停止游戏","强制停止当前游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null) {
				g.forceStop();
			}
			event.getRoom().sendMessage("已经停止正在进行的游戏！");
			event.getSender().sendPrivate("已经停止正在进行的游戏！");
		});
		addPCmd("暂停游戏","强制停止当前游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null) {
				g.forceInterrupt();
			}
			event.getSender().sendPrivate("已经暂停正在进行的游戏！");
		});
		addPCmd("强制报名","<qq>强制玩家报名当前游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.addMember(event.getRoom().get(UserIdentifierSerializer.read(args[1])));
			}
		});
		addPCmd("继续游戏","继续暂停的游戏",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				event.getRoom().sendMessage("因为有其他的游戏正在运行，无法继续。");
				return;
			}
			try(FileInputStream fileOut = new FileInputStream(new File(dataFolder,event.getRoom()+".game"));ObjectInputStream out = new ObjectInputStream(fileOut)){
				GameUtils.getGames().put(event.getRoom(),(Game) out.readObject());
			} catch (IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				event.getRoom().sendMessage("继续游戏失败！");
				e.printStackTrace();
			}
		});
		Random ckr=new SecureRandom();
		List<String> cards=new ArrayList<>();
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
		addPCmd("狼人杀摇号","模拟狼人杀摇号",(event,args)->{
			List<Role> larr=WerewolfGame.fairRollRole(Integer.parseInt(args[1]));
			double pts=WerewolfGame.calculateRolePoint(larr);
			StringBuilder sb=new StringBuilder();
			for(Role cls:larr) {
				sb.append(cls.getName()).append(" ");
			}
			sb.append("得分：").append(pts);
			event.getRoom().sendMessage(sb.toString());
		});
		addCmd("狼人杀抽卡","进行一次虚拟抽卡",(event,args)->{
			event.getSender().sendPublic(cards.get(ckr.nextInt(cards.size())));
		});
		addCmd("成语接龙","开始成语接龙",(event,args)->{
			IdiomSolitare is=GameUtils.createGame(IdiomSolitare::new,event.getRoom(),1);
			is.startEmpty();
		});
		makeGame("狼人杀",WerewolfPreserve.class,new DefaultGameCreater<>(WerewolfGame.class));
		makeGame("小型狼人杀",MiniWerewolfPreserve.class,new DefaultGameCreater<>(WerewolfGame.class));
		makeGame("标准狼人杀",StandardWerewolfPreserve.class,new StandardWerewolfCreater());
		makeGame("谁是卧底",UnderCoverPreserve.class,new DefaultGameCreater<>(UnderCoverGame.class));
		makeGame("谁是卧底发词",UnderCoverHolderPreserve.class,new DefaultGameCreater<>(UnderCoverHolder.class));
		makeGame("妙探寻凶",CluePreserve.class,new DefaultGameCreater<>(ClueGame.class));
		makeGame("妙探寻凶X",FastCluePreserve.class,new DefaultGameCreater<>(FastClueGame.class));
		makeGame("SP战纪",SpWarframePreserve.class,new DefaultGameCreater<>(SpWarframe.class));
		makeGame("成语接龙",SolitarePreserve.class,new DefaultGameCreater<>(IdiomSolitare.class));
	}
	
}
