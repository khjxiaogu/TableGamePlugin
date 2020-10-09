package com.khjxiaogu.TableGames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.data.GenericPlayerData;
import com.khjxiaogu.TableGames.data.PlayerDatabase;
import com.khjxiaogu.TableGames.undercover.UnderCoverGame;
import com.khjxiaogu.TableGames.undercover.UnderCoverPreserve;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.utils.PreserveInfo;
import com.khjxiaogu.TableGames.utils.Utils;
import com.khjxiaogu.TableGames.werewolf.Villager;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.werewolf.WerewolfPreserve;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.TempMessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol;


public class TableGames extends PluginBase {
	public static TableGames plugin;
	public static PlayerDatabase db;
	public static Map<String,BiConsumer<GroupMessageEvent,String[]>> normcmd=new ConcurrentHashMap<>();
	public static Map<String,BiConsumer<GroupMessageEvent,String[]>> privcmd=new ConcurrentHashMap<>();
	public static ExecutorService dispatchexec=Executors.newCachedThreadPool();
	public static <T extends Game> void makeGame(String name,Class<? extends PreserveInfo<T>> preserver,Class<T> gameClass) {
		normcmd.put("预定"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).addPreserver(event.getSender());
		});
		normcmd.put("设置"+name+"参数", (event,command)->{
			String[] args=Arrays.copyOfRange(command,1,command.length);
			Utils.getPreserve(event.getGroup(),preserver).setArgs(args);
			event.getGroup().sendMessage("已设置参数为"+String.join(",",args));
			event.getGroup().sendMessage("特殊场已经设置，欢迎 @我 预定狼人杀 参与特殊场。");
		});
		normcmd.put(name+"统计", (event,command)->{
			event.getGroup().sendMessage(new At(event.getSender()).plus(db.getPlayer(event.getSender().getId(),name).toString()));
		});
		normcmd.put("取消预定"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).removePreserver(event.getSender());
		});
		privcmd.put("立即开始"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).startNow();
		});
		privcmd.put("强制预定"+name,(event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).addPreserver(event.getGroup().get(Long.parseLong(command[1])));
		});
		privcmd.put(name+"统计t", (event,command)->{
			event.getGroup().sendMessage(new At(event.getSender()).plus(db.getGame(name).getPlayer(event.getSender().getId(),PlayerDatabase.datacls.get(name)).toString()));
		});
		privcmd.put("b"+name, (event,command)->{
			PreserveInfo<?> pi=Utils.getPreserve(event.getGroup(),preserver);
			pi.enablefake=!pi.enablefake;
		});
		privcmd.put(name+"全局统计", (event,command)->{
			GenericPlayerData<? extends GenericPlayerData<?>>[] ds=db.getPlayers(name);
			for(int i=1;i<ds.length;i++) {
				ds[0].plusa(ds[i]);
			}
			event.getGroup().sendMessage("全局"+ds[0].toString());
		});
		privcmd.put("开始"+name, (event,command)->{
			Utils.createGame(gameClass,event.getGroup(),Integer.parseInt(command[1]));
			event.getGroup().sendMessage(name+"游戏已经创建，请 @我 报名 来报名。");
		});
		privcmd.put("定制"+name, (event,command)->{
			Utils.createGame(gameClass,event.getGroup(),Arrays.copyOfRange(command,1,command.length));
			event.getGroup().sendMessage(name+"游戏已经创建，请 @我 报名 来报名。");
		});
		
	}

	static {
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
		normcmd.put("狼人杀抽卡",(event,args)->{
			event.getGroup().sendMessage(new At(event.getSender()).plus(cards.get(ckr.nextInt(cards.size()))));
		});
		privcmd.put("狼人杀摇号",(event,args)->{
			List<Class<? extends Villager>> larr=WerewolfGame.fairRollRole(Integer.parseInt(args[1]));
			double pts=WerewolfGame.calculateRolePoint(larr);
			StringBuilder sb=new StringBuilder();
			for(Class<? extends Villager> cls:larr) {
				sb.append(WerewolfGame.getName(cls)).append(" ");
			}
			sb.append("得分：").append(pts);
			event.getGroup().sendMessage(sb.toString());
		});
		privcmd.put("TTI",(event,args)->{
			event.getGroup().sendMessage(Utils.sendTextAsImage(String.join(" ",args), event.getGroup()));
		});
		makeGame("狼人杀",WerewolfPreserve.class,WerewolfGame.class);
		makeGame("谁是卧底",UnderCoverPreserve.class,UnderCoverGame.class);
	}
	public static void transfer(InputStream i,OutputStream o) throws IOException {
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1)
				o.write(data, 0, nRead);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	public void onEnable() {
		BotConfiguration.getDefault().setProtocol(MiraiProtocol.ANDROID_PHONE);
		plugin=this;
		db=new PlayerDatabase(super.getDataFolder());
		try {
			File f=new File(super.getDataFolder(),"undtext.txt");
			if(!f.exists()) {
				f.createNewFile();
				FileOutputStream fos=new FileOutputStream(f);
				transfer(this.getResources("undtext.txt"),fos);
				fos.close();
			}
			
			getLogger().info("已载入"+UnderCoverTextLibrary.read(new FileInputStream(new File(super.getDataFolder(),"undtext.txt")))+"词条");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.getEventListener().subscribeAlways(net.mamoe.mirai.event.events.NewFriendRequestEvent.class,event->event.accept());
		this.getEventListener().subscribeAlways(GroupMessageEvent.class, event -> {
			if(event.getGroup().getBotAsMember().getPermission()==MemberPermission.MEMBER)return;
			At at = event.getMessage().first(At.Key);
			
			if (at!=null&&at.getTarget() == event.getBot().getId()) {
				Utils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.AT,event.getMessage());
				{
					String command=Utils.getPlainText(event.getMessage());
					String[] args=command.split(" ");
					BiConsumer<GroupMessageEvent,String[]> bae=normcmd.get(args[0]);
					if(bae!=null) {
						bae.accept(event,args);
					}else if(args[0].startsWith("报名")) {
						Game g=Utils.getGames().get(event.getGroup());
						if(g!=null&&g.isAlive())
							g.addMember(event.getSender());
					}else if(event.getSender().getPermission().getLevel()>0) {
						BiConsumer<GroupMessageEvent,String[]> bce=privcmd.get(args[0]);
						if(bce!=null) {
							bce.accept(event,args);
						}else if(command.startsWith("强制开始")) {
							Game g=Utils.getGames().get(event.getGroup());
							if(g!=null&&g.isAlive())
								g.forceStart();
						}else if(command.startsWith("停止游戏")) {
							Game g=Utils.getGames().get(event.getGroup());
							if(g!=null)
								g.forceStop();
							event.getGroup().sendMessage("已经停止正在进行的游戏！");
							event.getSender().sendMessage("已经停止正在进行的游戏！");
						}else if(args[0].startsWith("强制报名")) {
							Game g=Utils.getGames().get(event.getGroup());
							if(g!=null&&g.isAlive())
								g.addMember(event.getGroup().get(Long.parseLong(args[1])));
						}else if(args[0].startsWith("执行")) {
							dispatchexec.execute(()->Utils.dispatch(Long.parseLong(args[1]),MsgType.valueOf(args[2]),new MessageChainBuilder().append(args[3]).asMessageChain()));
						}
					}
				}
			}else {
				dispatchexec.execute(()->Utils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.PUBLIC,event.getMessage()));
			}
		});
		this.getEventListener().subscribeAlways(TempMessageEvent.class, event -> {dispatchexec.execute(()->Utils.dispatch(event.getSender().getId(),MsgType.PRIVATE,event.getMessage()));});
		this.getEventListener().subscribeAlways(FriendMessageEvent.class, event -> {dispatchexec.execute(()->Utils.dispatch(event.getSender().getId(),MsgType.PRIVATE,event.getMessage()));});
	}

}
