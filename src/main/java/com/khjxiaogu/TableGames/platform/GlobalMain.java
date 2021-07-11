package com.khjxiaogu.TableGames.platform;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.khjxiaogu.TableGames.game.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.game.werewolf.WerewolfPreserve;
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
	@FunctionalInterface
	interface BotCreater{
		AbstractBotUser createBot(int id,Class<? extends BotUserLogic> logicCls,Game in);
	}
	private static BotCreater defaultBotCreater;
	public static UnifiedLogger getLogger() {
		return logger;
	}
	public static void Init(File dataFolder) {
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
	public static Map<String,BiConsumer<RoomMessageEvent,String[]>> pvmgcmd=new ConcurrentHashMap<>();
	public static Map<String,BiConsumer<RoomMessageEvent,String[]>> privcmd=new ConcurrentHashMap<>();
	public static ExecutorService dispatchexec=Executors.newCachedThreadPool();
	public static List<String> gameList=new ArrayList<>();
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
				long id=Long.parseLong(command[1]);
				event.getRoom().sendMessage(event.getRoom().get(id).getNameCard()+"的"+GlobalMain.db.getPlayer(id,name).toString());
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
			PreserveHolder.getPreserve(event.getRoom(),preserver).addPreserver(event.getRoom().get(Long.parseLong(command[1])));
		});
		privcmd.put("强制取消预定"+name,(event,command)->{
			PreserveHolder.getPreserve(event.getRoom(),preserver).removePreserver(event.getRoom().get(Long.parseLong(command[1])),true);
		});
		privcmd.put(name+"统计", (event,command)->{
			event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(db.getGame(name).getPlayer(event.getSender().getId(),PlayerDatabase.datacls.get(name)).toString()));
		});
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
			PreserveHolder.getPreserves(event.getSender(),preserver).forEach(p->{
				t.success |=p.addConfig(event.getSender(),command[1],command[2]);
				getLogger().debug("setting");
			});
			if(t.success)
				event.getSender().sendPrivate("设置成功！");
			else
				event.getSender().sendPrivate("设置失败！");
		});

	}

	static {
		privcmd.put("揭示",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.forceShow( event.getSender());
			}
		});
		privcmd.put("跳过",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				g.forceSkip();
			}
		});
		privcmd.put("接管",(event,args)->{
			Game g=GameUtils.getGames().get(event.getRoom());
			if(g!=null&&g.isAlive()) {
				long m1=Long.parseLong(args[1]);
				AbstractUser m2=null;
				if(args.length>2) {
					m2=event.getRoom().get(Long.parseLong(args[2]));
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
		normcmd.put("查询积分",(event,args)->{
			event.getRoom().sendMessage(event.getSender().getAt().asMessage().append(credit.get(event.getSender().getId()).toString()));
		});
		normcmd.put("积分商城",(event,args)->{
			event.getRoom().sendMessage(CreditTrade.getList());
		});
		normcmd.put("购买",(event,args)->{
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
		privcmd.put("给积分",(event,args)->{
			double crp=GlobalMain.credit.get(Long.parseLong(args[1])).givePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("添加成功，现有"+crp+"积分");
		});
		privcmd.put("扣积分",(event,args)->{
			double crp=GlobalMain.credit.get(Long.parseLong(args[1])).removePT(Double.parseDouble(args[2]));
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"积分");
		});
		privcmd.put("禁赛",(event,args)->{
			GlobalMain.credit.get(Long.parseLong(args[1])).addBan(1000*3600*Integer.parseInt(args[2]));
			event.getRoom().sendMessage("已经禁赛到"+new Date(GlobalMain.credit.get(Long.parseLong(args[1])).isBanned()).toString());
		});
		privcmd.put("解除禁赛114514",(event,args)->{
			GlobalMain.credit.get(Long.parseLong(args[1])).removeBan();
			event.getRoom().sendMessage("已经解除！");
		});
		privcmd.put("使用积分",(event,args)->{
			double crp;
			if((crp=GlobalMain.credit.get(Long.parseLong(args[1])).withdrawPT(Integer.parseInt(args[2])))<0) {
				event.getRoom().sendMessage("扣除失败，积分还差"+-crp+"点");
			}
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"积分");
		});
		privcmd.put("给物品",(event,args)->{
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			int crp=GlobalMain.credit.get(Long.parseLong(args[1])).giveItem(args[2],cnt);
			event.getRoom().sendMessage("添加成功，现有"+crp+"个"+args[2]);
		});
		privcmd.put("扣物品",(event,args)->{
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			int crp=GlobalMain.credit.get(Long.parseLong(args[1])).removeItem(args[2],cnt);
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"个"+args[2]);
		});
		privcmd.put("使用物品",(event,args)->{
			int crp;
			int cnt=args.length>3?Integer.parseInt(args[3]):1;
			if((crp=GlobalMain.credit.get(Long.parseLong(args[1])).withdrawItem(args[2],cnt))<0) {
				event.getRoom().sendMessage("扣除失败，"+args[2]+"还差"+-crp+"个");
			}
			event.getRoom().sendMessage("扣除成功，还剩"+crp+"个"+args[2]);
		});
		normcmd.put("游戏列表", (event,args)->{
			StringBuilder sb=new StringBuilder("可用的游戏：\n");
			sb.append(String.join("，",gameList));
			sb.append("\n欢迎使用“##预定(游戏名)”预定！");
			event.getRoom().sendMessage(sb.toString());
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
