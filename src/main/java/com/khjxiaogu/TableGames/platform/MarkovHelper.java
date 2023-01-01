package com.khjxiaogu.TableGames.platform;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.platform.Markov.StateContainer;
import com.khjxiaogu.TableGames.utils.Utils;

public class MarkovHelper {

	public MarkovHelper() {
	}
	private static Markov mc=new Markov();
	public static Map<UserIdentifier,StateContainer> states=new HashMap<>();
	public static Set<UserIdentifier> ergroup=new HashSet<>();
	public static Set<UserIdentifier> ignores=new HashSet<>();
	public static Set<UserIdentifier> nlgroup=new HashSet<>();
	public static void loadConfig(File datafolder) {
		try (FileInputStream fis=new FileInputStream(new File(datafolder,"markov.json"))){
			JsonObject jo=JsonParser.parseString(new String(Utils.readAll(fis),StandardCharsets.UTF_8)).getAsJsonObject();
			addAll(nlgroup,jo.get("ignoregroups").getAsJsonArray());
			addAll(ergroup,jo.get("enablegroups").getAsJsonArray());
			addAll(ignores,jo.get("ignoreacounts").getAsJsonArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private static void addAll(Set<UserIdentifier> set,JsonArray ja) {
		for(JsonElement je:ja) {
			set.add(UserIdentifierSerializer.read(je.getAsString()));
		}
	}
	public static String handleMarkov(String command,UserIdentifier group,UserIdentifier uid) {
		if(ergroup.contains(group)&&!ignores.contains(uid)) {
			String r=null;
			if(!command.startsWith("!!")&&!command.startsWith("Mk$")) {
				if(!command.startsWith("#rb")) {
					if(command.startsWith("#?")) {
						
						r="#rb<header>3> 要求改写对应信息\r\n"
						+ "#srb<seed> <header>3>以固定种子生成信息\r\n"
						+ "#gnr[seed]以固定种子或者随机种子直接生成一段文本\r\n"
						+ "#? 显示此消息";
					}else if(command.startsWith("#srb")) {
						String cmd=Utils.removeLeadings("#srb",command);
						int cid=cmd.indexOf(" ");
						String seed=cmd.substring(0,cid);
						String text=cmd.substring(cid+1);
						r=mc.sfret(text,seed);
					}else if(command.startsWith("#gnr")) {
						String cmd=Utils.removeLeadings("#gnr",command);
						r=mc.gar(cmd);
					}
					else if(!command.startsWith("#")) r=mc.ret(command,states.computeIfAbsent(group,a->new StateContainer()));
				}else
					r=mc.fret(Utils.removeLeadings("#rb",command));
				return r;
			}
		}else if(!nlgroup.contains(group)) mc.train(command,states.computeIfAbsent(group,a->new StateContainer()));
		return null;
	}
}
