package com.khjxiaogu.TableGames.platform.mirai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.PluginData;
import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomLibrary;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MessageListener.MsgType;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Game;
import com.khjxiaogu.TableGames.utils.GameUtils;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.events.StrangerMessageEvent;
import net.mamoe.mirai.message.data.At;


public class MiraiMain extends JavaPlugin {
	public MiraiMain() {
		super(new JvmPluginDescriptionBuilder(PluginData.id,PluginData.version).name(PluginData.name).author(PluginData.author).info(PluginData.info).build());
	}
	public static MiraiMain plugin;


	public static void transfer(InputStream i,OutputStream o) throws IOException {
		int nRead;
		byte[] data = new byte[4096];

		try {
			while ((nRead = i.read(data, 0, data.length)) != -1) {
				o.write(data, 0, nRead);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}
	}
	@Override
	public void onEnable() {
		GlobalMain.setLogger(new MiraiGameLogger(this.getLogger()));
		//BotConfiguration.getDefault().setProtocol(MiraiProtocol.ANDROID_PHONE);
		MiraiMain.plugin=this;
		GlobalMain.Init(super.getDataFolder());
		
		try {
			File f=new File(super.getDataFolder(),"undtext.txt");
			File f2=new File(super.getDataFolder(),"cyyy.csv");
			if(!f.exists()) {
				f.createNewFile();
				FileOutputStream fos=new FileOutputStream(f);
				MiraiMain.transfer(getResourceAsStream("undtext.txt"),fos);
				fos.close();
			}
			if(!f2.exists()) {
				f2.createNewFile();
				FileOutputStream fos=new FileOutputStream(f2);
				MiraiMain.transfer(getResourceAsStream("cyyy.csv"),fos);
				fos.close();
			}
			try (FileInputStream fis=new FileInputStream(new File(super.getDataFolder(),"undtext.txt"))){
				getLogger().info("[谁是卧底]已载入"+UnderCoverTextLibrary.read(fis)+"词条");
			}
			try (FileInputStream fis=new FileInputStream(new File(super.getDataFolder(),"cyyy.csv"))){
				getLogger().info("[成语接龙]已载入"+IdiomLibrary.read(fis)+"词条");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GlobalEventChannel.INSTANCE.registerListenerHost(new SimpleListenerHost(getCoroutineContext()) {
			@EventHandler
			public void onGroup(GroupMessageEvent event) {
				//if(event.getGroup().getBotAsMember().getPermission()==MemberPermission.MEMBER)return;
				
				At at = MiraiUtils.getAt(event.getMessage());
				String command=MiraiUtils.getPlainText(event.getMessage());
				boolean hasCmd=false;
				if((at!=null&&at.getTarget() == event.getBot().getId())) {
					hasCmd=true;
				}else if(command.startsWith("##")) {
					hasCmd=true;
					command=Utils.removeLeadings("##",command);
				}
					
				if (hasCmd) {
					MiraiListenerUtils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.AT,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot()));
					{
						
						String[] args=command.split(" ");
						
						BiConsumer<RoomMessageEvent, String[]> bae=GlobalMain.normcmd.get(args[0]);
						if(bae!=null) {
							MiraiRoomMessageEvent uev=new MiraiRoomMessageEvent(event);
							bae.accept(uev,args);
						}else if(args[0].startsWith("报名")) {
							Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
							if(g!=null&&g.isAlive()) {
								g.addMember(new MiraiHumanUser((NormalMember) event.getSender()));
							}
						}else if(event.getSender().getPermission().getLevel()>0) {
							BiConsumer<RoomMessageEvent, String[]> bce=GlobalMain.privcmd.get(args[0]);
							if(bce!=null) {
								MiraiRoomMessageEvent uev=new MiraiRoomMessageEvent(event);
								bce.accept(uev,args);
							}else if(command.startsWith("强制开始")) {
								Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
								if(g!=null&&g.isAlive()) {
									g.forceStart();
								}
							}else if(command.startsWith("停止游戏")) {
								Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
								if(g!=null) {
									g.forceStop();
								}
								event.getGroup().sendMessage("已经停止正在进行的游戏！");
								event.getSender().sendMessage("已经停止正在进行的游戏！");
							}else if(command.startsWith("暂停游戏")) {
								Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
								if(g!=null) {
									g.forceInterrupt();
								}
								event.getSender().sendMessage("已经暂停正在进行的游戏！");
							}else if(command.startsWith("继续游戏")) {
								Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
								if(g!=null&&g.isAlive()) {
									event.getGroup().sendMessage("因为有其他的游戏正在运行，无法继续。");
									return;
								}
								try(FileInputStream fileOut = new FileInputStream(new File(MiraiMain.plugin.getDataFolder(),""+MiraiGroup.createInstance(event.getGroup())+".game"));ObjectInputStream out = new ObjectInputStream(fileOut)){
									
									GameUtils.getGames().put(MiraiGroup.createInstance(event.getGroup()),(Game) out.readObject());
								} catch (IOException | ClassNotFoundException e) {
									// TODO Auto-generated catch block
									event.getGroup().sendMessage("继续游戏失败！");
									e.printStackTrace();
								}

							}else if(args[0].startsWith("强制报名")) {
								Game g=GameUtils.getGames().get(MiraiGroup.createInstance(event.getGroup()));
								if(g!=null&&g.isAlive()) {
									g.addMember(new MiraiHumanUser(event.getGroup().get(Long.parseLong(args[1]))));
								}
							}else if(args[0].startsWith("执行")) {
								GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(Long.parseLong(args[1]),MsgType.valueOf(args[2]),new Text(args[3]).asMessage()));
							}
						}
					}
				}else {
					GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(event.getSender().getId(),event.getGroup(),MsgType.PUBLIC,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot())));
				}
			}
			@EventHandler
			public void onNewFriend(NewFriendRequestEvent ev) {
				ev.accept();
			}
			@EventHandler
			public void onFriend(FriendMessageEvent event) {
				String command=MiraiUtils.getPlainText(event.getMessage());
				if(command.startsWith("##")) {
					command=Utils.removeLeadings("##",command);
					String[] args=command.split(" ");
					BiConsumer<RoomMessageEvent, String[]> bae=GlobalMain.pvmgcmd.get(args[0]);
					if(bae!=null) {
						MiraiPrivateMessageEvent uev=new MiraiPrivateMessageEvent(event);
						bae.accept(uev,args);
					}
				}
				GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(event.getSender().getId(),MsgType.PRIVATE,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot())));
			}

			@EventHandler
			public void onTemp(StrangerMessageEvent event) {
				String command=MiraiUtils.getPlainText(event.getMessage());
				if(command.startsWith("##")) {
					command=Utils.removeLeadings("##",command);
					String[] args=command.split(" ");
					BiConsumer<RoomMessageEvent, String[]> bae=GlobalMain.pvmgcmd.get(args[0]);
					if(bae!=null) {
						MiraiPrivateMessageEvent uev=new MiraiPrivateMessageEvent(event);
						bae.accept(uev,args);
					}
				}
				GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(event.getSender().getId(),MsgType.PRIVATE,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot())));
			}
		});
	}

}
