package com.khjxiaogu.TableGames.platform.simplerobot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.khjxiaogu.TableGames.game.idiomsolitare.IdiomLibrary;
import com.khjxiaogu.TableGames.game.undercover.UnderCoverTextLibrary;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.MarkovHelper;
import com.khjxiaogu.TableGames.platform.SBId;
import com.khjxiaogu.TableGames.platform.message.IMessageCompound;
import com.khjxiaogu.TableGames.utils.Utils;

import kotlin.Unit;
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

	public static String token = System.getProperty("kooktoken");
	public static String client = System.getProperty("kookclient");
	public static KookAPI api = new KookAPI(token);

	@SuppressWarnings("resource")
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
				transfer(KookMain.class.getResourceAsStream("/undtext.txt"), fos);
				fos.close();
			}
			if (!f2.exists()) {
				f2.createNewFile();
				FileOutputStream fos = new FileOutputStream(f2);
				transfer(KookMain.class.getResourceAsStream("/cyyy.csv"), fos);
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
					GlobalMain.defaultFirePrivate(SBUtils.getPlainText(event.getMessageContent().getMessages()),
							SBId.of(event.getUser().getId()),
							(IMessageCompound) KooKAdapter.INSTANCE.toUnified(event.getMessageContent().getMessages(), event.getBot()),
							() -> new SBPrivateMessageEvent((KookContactMessageEvent) event));
					return EventResult.defaults();
				});
		EventListener room = SimpleListeners.listener(/* target = */ ChannelMessageEvent.Key,
				/* invoker = */ (context, event) -> {
					try {
						String command = SBUtils.getPlainText(event.getMessageContent().getMessages());
						SBId sid=SBId.of(event.getAuthor().getId());
						SBId rid=SBId.of(event.getChannel().getId());
						String s=MarkovHelper.handleMarkov(command,rid,sid);
						GlobalMain.getLogger().info(event.toString());
						if(s!=null) {
							event.getChannel().sendAsync(s);
						}
						At at = SBUtils.getAt(event.getMessageContent().getMessages());
						boolean hasCmd = false;
						
						if ((at != null && event.getBot().isMe(at.getTarget()))) {
							hasCmd = true;
						} else if (command.startsWith("##")) {
							hasCmd = true;
							command = Utils.removeLeadings("##", command);
						}
						GlobalMain.firePublicCommand(hasCmd?command:null,sid,()->new SBHumanUser((GuildMember) event.getAuthor(),event.getChannel()),()->new SBRoomMessageEvent(event),rid, (IMessageCompound) KooKAdapter.INSTANCE
								.toUnified(event.getMessageContent().getMessages(), event.getBot()));
						
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
