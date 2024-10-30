package com.khjxiaogu.TableGames.platform;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.data.CachedDataMap;
import com.khjxiaogu.TableGames.data.CachedSet;
import com.khjxiaogu.TableGames.platform.Markov.StateContainer;
import com.khjxiaogu.TableGames.utils.Utils;

public class MarkovHelper {

	public MarkovHelper() {
	}
	private static Markov def=new Markov(new File("markov.db"));
	public static Map<String,StateContainer> states=new HashMap<>();
	public static Map<String,String> prob=new HashMap<>();
	public static Map<String,String> prof=new HashMap<>();
	public static Set<String> ergroup=new HashSet<>();
	public static Set<String> ignores=new HashSet<>();
	public static Set<String> nlgroup=new HashSet<>();
	public static Map<String,Markov> profiles=new ConcurrentHashMap<>();
	static Connection database;
	public static File profs;
	public static void loadConfig(File datafolder) {
		def.readOnly=false;
		profs=new File("markov");
		profs.mkdirs();
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			return;
		}
		try {
			database = DriverManager.getConnection("jdbc:sqlite:" + new File(datafolder,"markov-settings.db"));
			ergroup=new CachedSet(database,"enabledgroups");
			ignores=new CachedSet(database,"ignoreaccounts");
			nlgroup=new CachedSet(database,"ignoredgroups");
			prob=new CachedDataMap(database,"replychance");
			prof=new CachedDataMap(database,"profiles");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		prof.put("default", "default");
		GlobalMain.expireTasks.add(e->profiles.keySet().removeIf(x->!prof.containsValue(x)));
		profiles.put("default",def);
		/*try (FileInputStream fis=new FileInputStream(new File(datafolder,"markov.json"))){
			JsonObject jo=JsonParser.parseString(new String(Utils.readAll(fis),StandardCharsets.UTF_8)).getAsJsonObject();
			addAll(nlgroup,jo.get("ignoregroups").getAsJsonArray());
			addAll(ergroup,jo.get("enablegroups").getAsJsonArray());
			addAll(ignores,jo.get("ignoreacounts").getAsJsonArray());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
	}
	private static void addAll(Set<String> set,JsonArray ja) {
		for(JsonElement je:ja) {
			set.add(je.getAsString());
		}
	}
	public static Markov getMarkov(String group) {
		String g=prof.get(group);
		if(g!=null) {
			return profiles.computeIfAbsent(g,d->new Markov(new File(profs,d+".db")));
		}
		return def;
	}
	private static boolean shouldTalk(String group) {
		String f=prob.get(group);
		if(f==null)return true;
		try {
			return Math.random()<Float.parseFloat(f);
		}catch(Exception ex) {
			return true;
		}
	}
	public static List<String> profiles() {
		List<String> li=Arrays.stream(profs.list((d,e)->e.endsWith(".db"))).map(s->s.substring(0,s.lastIndexOf("."))).collect(Collectors.toList());
		li.add("default");
		return li;
	}
	
	public static String handleMarkov(String command,UserIdentifier group,UserIdentifier uid) {
		String gn=group.serialize();
		if(ergroup.contains(gn)&&!ignores.contains(uid.serialize())) {
			String r=null;
			if(!command.startsWith("!!")&&!command.startsWith("Mk$")) {
				if(!command.startsWith("#rb")) {
					if(command.startsWith("#srb")) {
						String cmd=Utils.removeLeadings("#srb",command);
						int cid=cmd.indexOf(" ");
						String seed=cmd.substring(0,cid);
						String text=cmd.substring(cid+1);
						r=getMarkov(gn).sfret(text,seed);
					}else if(command.startsWith("#gnr")) {
						String cmd=Utils.removeLeadings("#gnr",command);
						r=getMarkov(gn).gar(cmd);
					}
					else if(!command.startsWith("#")) {
						if(shouldTalk(gn))
							r=getMarkov(gn).ret(command,states.computeIfAbsent(gn,a->new StateContainer()));
						else
							getMarkov(gn).train(command,states.computeIfAbsent(gn,a->new StateContainer()));
					}
				}else
					r=getMarkov(gn).fret(Utils.removeLeadings("#rb",command));
				return r;
			}
		}else if(!nlgroup.contains(gn)) def.train(command,states.computeIfAbsent(gn,a->new StateContainer()));
		return null;
	}
}
