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
package com.khjxiaogu.TableGames.platform.mirai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import com.khjxiaogu.TableGames.PluginData;
import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomLibrary;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
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
		GlobalMain.init(super.getDataFolder());
		GlobalMain.privmatcher.load(this.getDataFolder());
		UserIdentifierSerializer.addRawSerializer(e->QQId.of(Long.parseLong(e)));
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
					MiraiListenerUtils.dispatch(event.getSender(),event.getGroup(),MsgType.AT,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot()));
					{
						
						String[] args=command.split(" ");
						
						BiConsumer<RoomMessageEvent, String[]> bae=GlobalMain.normcmd.get(args[0]);
						if(bae!=null) {
							MiraiRoomMessageEvent uev=new MiraiRoomMessageEvent(event);
							bae.accept(uev,args);
						}else if(GlobalMain.privmatcher.match(new MiraiHumanUser((NormalMember) event.getSender())).isAllowed()) {
							BiConsumer<RoomMessageEvent, String[]> bce=GlobalMain.privcmd.get(args[0]);
							if(bce!=null) {
								MiraiRoomMessageEvent uev=new MiraiRoomMessageEvent(event);
								bce.accept(uev,args);
							}else if(args[0].startsWith("执行")) {
								GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(Long.parseLong(args[1]),MsgType.valueOf(args[2]),new Text(args[3]).asMessage()));
							}
						}
					}
				}else {
					GlobalMain.dispatchexec.execute(()->MiraiListenerUtils.dispatch(event.getSender(),event.getGroup(),MsgType.PUBLIC,(IMessageCompound) MiraiAdapter.INSTANCE.toUnified(event.getMessage(),event.getBot())));
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
