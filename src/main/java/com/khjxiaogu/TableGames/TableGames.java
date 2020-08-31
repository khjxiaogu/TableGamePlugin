package com.khjxiaogu.TableGames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.MessageListener.MsgType;
import com.khjxiaogu.TableGames.undercover.UnderCoverGame;
import com.khjxiaogu.TableGames.undercover.UnderCoverPreserve;
import com.khjxiaogu.TableGames.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.werewolf.Crow;
import com.khjxiaogu.TableGames.werewolf.Demon;
import com.khjxiaogu.TableGames.werewolf.GraveKeeper;
import com.khjxiaogu.TableGames.werewolf.Defender;
import com.khjxiaogu.TableGames.werewolf.Hunter;
import com.khjxiaogu.TableGames.werewolf.Idiot;
import com.khjxiaogu.TableGames.werewolf.Villager;
import com.khjxiaogu.TableGames.werewolf.Knight;
import com.khjxiaogu.TableGames.werewolf.Elder;
import com.khjxiaogu.TableGames.werewolf.Seer;
import com.khjxiaogu.TableGames.werewolf.Tramp;
import com.khjxiaogu.TableGames.werewolf.WerewolfGame;
import com.khjxiaogu.TableGames.werewolf.WerewolfPreserve;
import com.khjxiaogu.TableGames.werewolf.WhiteWolf;
import com.khjxiaogu.TableGames.werewolf.Witch;
import com.khjxiaogu.TableGames.werewolf.Werewolf;
import com.khjxiaogu.TableGames.werewolf.WolfKiller;

import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.FriendMessageEvent;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.message.TempMessageEvent;
import net.mamoe.mirai.message.data.At;

public class TableGames extends PluginBase {
	public static Map<String,BiConsumer<GroupMessageEvent,String[]>> normcmd=new ConcurrentHashMap<>();
	public static Map<String,BiConsumer<GroupMessageEvent,String[]>> privcmd=new ConcurrentHashMap<>();
	public static <T extends Game> void makeGame(String name,Class<? extends PreserveInfo<T>> preserver,Class<T> gameClass) {
		normcmd.put("预定"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).addPreserver(event.getSender());
		});
		normcmd.put("取消预定"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).removePreserver(event.getSender());
		});
		privcmd.put("立即开始"+name, (event,command)->{
			Utils.getPreserve(event.getGroup(),preserver).startNow();
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
			event.getGroup().sendMessage(new At((Member)event.getSender()).plus(cards.get(ckr.nextInt(cards.size()))));
		});
		makeGame("狼人杀",WerewolfPreserve.class,WerewolfGame.class);
		makeGame("谁是卧底",UnderCoverPreserve.class,UnderCoverGame.class);
	}
	public void onEnable() {
		try {
			File f=new File(super.getDataFolder(),"undtext.txt");
			if(!f.exists()) {
				f.createNewFile();
				FileOutputStream fos=new FileOutputStream(f);
				this.getResources("undtext.txt").transferTo(fos);
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
				if(!Utils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.AT,event.getMessage())) {
					String command=Utils.getPlainText(event.getMessage());
					String[] args=command.split(" ");
					BiConsumer<GroupMessageEvent,String[]> bae=normcmd.get(args[0]);
					if(bae!=null) {
						bae.accept(event,args);
					}else if(args[0].startsWith("报名")) {
						Game g=Utils.gs.get(event.getGroup());
						if(g!=null&&g.isAlive())
							g.addMember(event.getSender());
					}else if(event.getSender().getPermission().getLevel()>0) {
						BiConsumer<GroupMessageEvent,String[]> bce=privcmd.get(args[0]);
						if(bce!=null) {
							bce.accept(event,args);
						}else if(command.startsWith("强制开始")) {
							Game g=Utils.gs.get(event.getGroup());
							if(g!=null&&g.isAlive())
								g.forceStart();
						}else if(command.startsWith("停止游戏")) {
							Game g=Utils.gs.get(event.getGroup());
							if(g!=null)
								g.forceStop();
							event.getGroup().sendMessage("已经停止正在进行的游戏！");
							event.getSender().sendMessage("已经停止正在进行的游戏！");
						}
					}
				}
			}else {
				Utils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.PUBLIC,event.getMessage());
			}
		});
		this.getEventListener().subscribeAlways(TempMessageEvent.class, event -> {Utils.dispatch(event.getSender().getId(),MsgType.PRIVATE,event.getMessage());});
		this.getEventListener().subscribeAlways(FriendMessageEvent.class, event -> {Utils.dispatch(event.getSender().getId(),MsgType.PRIVATE,event.getMessage());});
	}

}
