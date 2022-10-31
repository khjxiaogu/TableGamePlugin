package com.khjxiaogu.TableGames.platform.simplerobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import org.apache.logging.log4j.LogManager;

import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomLibrary;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MsgType;
import com.khjxiaogu.TableGames.platform.RoomMessageEvent;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.UserIdentifierSerializer;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.platform.message.Text;
import com.khjxiaogu.TableGames.utils.Utils;

import kotlin.Unit;
import love.forte.simbot.ID;
import love.forte.simbot.application.ApplicationDslBuilder;
import love.forte.simbot.application.Applications;
import love.forte.simbot.application.EventProvider;
import love.forte.simbot.component.kook.KookBotManager;
import love.forte.simbot.component.kook.KookComponent;
import love.forte.simbot.component.kook.KookComponentBot;
import love.forte.simbot.component.kook.event.KookContactMessageEvent;
import love.forte.simbot.core.application.Simple;
import love.forte.simbot.core.application.SimpleApplication;
import love.forte.simbot.core.application.SimpleApplicationBuilder;
import love.forte.simbot.core.application.SimpleApplicationConfiguration;
import love.forte.simbot.core.event.SimpleListeners;
import love.forte.simbot.definition.GuildMember;
import love.forte.simbot.event.ChannelMessageEvent;
import love.forte.simbot.event.ContactMessageEvent;
import love.forte.simbot.event.EventListener;
import love.forte.simbot.event.EventResult;
import love.forte.simbot.kook.KookBotConfiguration;
import love.forte.simbot.logger.LoggerFactory;
import love.forte.simbot.message.At;
import love.forte.simbot.utils.Lambdas;

public class KookMain {
	public static void transfer(InputStream i, OutputStream o) throws IOException {
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

	public static File getDataFolder() {
		return new File("./data/com.khjxiaogu.mirai.TableGames/");
	}


	public static KookAPI api = new KookAPI(token);

	public static void main(String[] programargs) {
		getDataFolder().mkdirs();

		GlobalMain.setLogger(new SBGameLogger(LoggerFactory.getLogger("TableGames")));
		GlobalMain.init(getDataFolder());
		GlobalMain.privmatcher.load(getDataFolder());
		try {
			File f = new File(getDataFolder(), "undtext.txt");
			File f2 = new File(getDataFolder(), "cyyy.csv");
			if (!f.exists()) {
				f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);
				transfer(KookMain.class.getResourceAsStream("undtext.txt"), fos);
				fos.close();
			}
			if (!f2.exists()) {
				f2.createNewFile();
				FileOutputStream fos = new FileOutputStream(f2);
				transfer(KookMain.class.getResourceAsStream("cyyy.csv"), fos);
				fos.close();
			}
			try (FileInputStream fis = new FileInputStream(new File(getDataFolder(), "undtext.txt"))) {
				GlobalMain.getLogger().info("[谁是卧底]已载入" + UnderCoverTextLibrary.read(fis) + "词条");
			}
			try (FileInputStream fis = new FileInputStream(new File(getDataFolder(), "cyyy.csv"))) {
				GlobalMain.getLogger().info("[成语接龙]已载入" + IdiomLibrary.read(fis) + "词条");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EventListener friend = SimpleListeners.listener(/* target = */ ContactMessageEvent.Key,
				/* invoker = */ (context, event) -> {
					try {
						String command = SBUtils.getPlainText(event.getMessageContent().getMessages());
						GlobalMain.getLogger().info(command);
						if (command.startsWith("##")) {
							command = Utils.removeLeadings("##", command);
							String[] args = command.split(" ");
							BiConsumer<RoomMessageEvent, String[]> bae = GlobalMain.pvmgcmd.get(args[0]);
							if (bae != null) {
								SBPrivateMessageEvent uev = new SBPrivateMessageEvent((KookContactMessageEvent) event);
								bae.accept(uev, args);
							}
						}
						GlobalMain.dispatchexec.execute(() -> SBListenerUtils.dispatch(event.getUser().getId(),
								MsgType.PRIVATE, (IMessageCompound) KooKAdapter.INSTANCE
										.toUnified(event.getMessageContent().getMessages(), event.getBot())));
						// 返回值可选, 如果不提供默认值则视为 EventResult.invalid() .
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return EventResult.defaults();
				});
		EventListener room = SimpleListeners.listener(/* target = */ ChannelMessageEvent.Key,
				/* invoker = */ (context, event) -> {
					try {
						At at = SBUtils.getAt(event.getMessageContent().getMessages());
						boolean hasCmd = false;
						String command = SBUtils.getPlainText(event.getMessageContent().getMessages());
						command=command.trim();
						GlobalMain.getLogger().info(command);
						if ((at != null && at.getTarget() == event.getBot().getId())) {
							hasCmd = true;
						} else if (command.startsWith("##")) {
							hasCmd = true;
							command = Utils.removeLeadings("##", command);
						}

						if (hasCmd) {

							SBListenerUtils.dispatch((GuildMember) event.getAuthor(), event.getChannel(), MsgType.AT,
									(IMessageCompound) KooKAdapter.INSTANCE
											.toUnified(event.getMessageContent().getMessages(), event.getBot()));
							{

								String[] args = command.split(" ");

								BiConsumer<RoomMessageEvent, String[]> bae = GlobalMain.normcmd.get(args[0]);
								if (bae != null) {
									SBRoomMessageEvent uev = new SBRoomMessageEvent(event);
									bae.accept(uev, args);
								} else if (GlobalMain.privmatcher
										.match(new SBHumanUser((GuildMember) event.getAuthor(), event.getChannel()))
										.isAllowed()) {
									BiConsumer<RoomMessageEvent, String[]> bce = GlobalMain.privcmd.get(args[0]);
									if (bce != null) {
										SBRoomMessageEvent uev = new SBRoomMessageEvent(event);
										bce.accept(uev, args);
									} else if (args[0].startsWith("执行")) {
										GlobalMain.dispatchexec.execute(() -> SBListenerUtils.dispatch(ID.$(args[1]),
												MsgType.valueOf(args[2]), new Text(args[3]).asMessage()));
									}
								}
							}
						} else {
							GlobalMain.dispatchexec.execute(() -> SBListenerUtils.dispatch(
									(GuildMember) event.getAuthor(), event.getChannel(), MsgType.PUBLIC,
									(IMessageCompound) KooKAdapter.INSTANCE
											.toUnified(event.getMessageContent().getMessages(), event.getBot())));
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					return EventResult.defaults();
				});
		ApplicationDslBuilder<SimpleApplicationConfiguration, SimpleApplicationBuilder, SimpleApplication> app = Applications
				.buildSimbotApplication(Simple.INSTANCE);

		app.build(Lambdas.suspendConsumer((builder, configuration) -> {
			builder.eventProcessor((eventProcessorConfiguration, environment) -> {
				// 直接添加监听函数
				eventProcessorConfiguration.addListener(room);
				eventProcessorConfiguration.addListener(friend);
				return Unit.INSTANCE;
			});
			builder.install(KookComponent.Factory, (config, perceivable) -> Unit.INSTANCE);
			builder.install(KookBotManager.Factory, (config, perceivable) -> Unit.INSTANCE);
			builder.bots(Lambdas.suspendConsumer(botRegistrar -> {
				for (EventProvider provider : botRegistrar.getProviders()) {
					if (provider instanceof KookBotManager) {
						KookBotManager botManager = (KookBotManager) provider;
						KookComponentBot bot = botManager.register(client, token, t -> {
							t.setBotConfiguration(new KookBotConfiguration());
							return Unit.INSTANCE;
						});
						// final MiraiBot bot = botManager;
						// bot.startBlocking();
						// or bot.startAsync()

						bot.startAsync();

						break;
					}
				}
			}));

		}));
		app.createBlocking().joinBlocking();
	}

}
