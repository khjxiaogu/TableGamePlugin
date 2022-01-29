package com.khjxiaogu.TableGames.permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import com.khjxiaogu.TableGames.platform.AbstractUser;
import com.khjxiaogu.TableGames.platform.GlobalMain;


public class GlobalMatcher implements PermissionMatcher{
	BotMatcher global;
	Map<Long,BotMatcher> local=new ConcurrentHashMap<>();
	@Override
	public PermissionResult match(AbstractUser m) {
		BotMatcher bm=local.getOrDefault(m.getHostId(),global);
		return bm.match(m);
	}

	@Override
	public PermissionResult match(AbstractUser u, boolean temp) {
		BotMatcher bm=local.getOrDefault(u.getHostId(),global);
		return bm.match(u,temp);
	}

	@Override
	public PermissionResult match(long id, long group, long botid) {
		BotMatcher bm=local.getOrDefault(botid,global);
		return bm.match(id,group,botid);
	}
	public void loadString(String s,long bid) {
		BotMatcher bm=local.get(bid);
		if(bm==null) {
			bm=new BotMatcher();
			local.put(bid,bm);
		}
		bm.loadMatcher(s);
		try(FileOutputStream fis=new FileOutputStream(new File(loadfrom,bid+".permission"),true);PrintStream sc=new PrintStream(fis)){
			sc.println();
			sc.print(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void loadString(String s) {
		global.loadMatcher(s);
		try(FileOutputStream fis=new FileOutputStream(new File(loadfrom,"global.permission"),true);PrintStream sc=new PrintStream(fis)){
			sc.println();
			sc.print(s);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public void rebuildConfig() {
		try(FileOutputStream fis=new FileOutputStream(new File(loadfrom,"global.permission"),false);PrintStream sc=new PrintStream(fis)){
			boolean nfirst=false;
			for(String s:global.getValue()) {
				if(nfirst)
					sc.println();
				nfirst=true;
				sc.print(s);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		for(Entry<Long, BotMatcher> i:local.entrySet()){
			try(FileOutputStream fis=new FileOutputStream(new File(loadfrom,i.getKey()+".permission"),false);PrintStream sc=new PrintStream(fis)){
				boolean nfirst=false;
				for(String s:global.getValue()) {
					if(nfirst)
						sc.println();
					nfirst=true;
					sc.print(s);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	public void reload() {
		load(loadfrom);
	}
	File loadfrom;
	public void load(File f) {
		loadfrom=f;
		global=null;
		local.clear();
		File gc=new File(f,"global.permission");
		global=new BotMatcher();
		if(!gc.exists()) {
			try {
				gc.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try(FileInputStream fis=new FileInputStream(gc);Scanner sc=new Scanner(fis)){
			int i=0;
			while(sc.hasNextLine()) {
				i++;
				try {
					global.loadMatcher(sc.nextLine());
				}catch(Exception ex) {
					//GlobalMain.getLogger().warning(ex);
					GlobalMain.getLogger().warning("权限配置文件"+gc.getName()+"的第"+i+"行有语法错误！");
				}
			}
		}catch(Exception ex) {
			//GlobalMain.getLogger().warning(ex);
			GlobalMain.getLogger().warning("权限配置文件"+gc.getName()+"读取失败！"+ex.getMessage());
			
		}
		for(File ff:f.listFiles()) {
			try {
			if(ff.getName().endsWith(".permission")) {
				String fn=ff.getName().split("\\.")[0];
				if(Character.isDigit(fn.charAt(0))) {
					long gn=Long.parseLong(fn);
					BotMatcher bm=new BotMatcher();
					try(FileInputStream fis=new FileInputStream(ff);Scanner sc=new Scanner(fis)){
						int i=0;
						while(sc.hasNextLine()) {
							i++;
							try {
								bm.loadMatcher(sc.nextLine());
							}catch(Exception ex) {
								//GlobalMain.getLogger().warning(ex);
								GlobalMain.getLogger().warning("权限配置文件"+ff.getName()+"的第"+i+"行有语法错误！");
							}
						}
					}
					local.put(gn,bm);
				}
			}
			}catch(Exception ex) {
				//GlobalMain.getLogger().warning(ex);
				GlobalMain.getLogger().warning("权限配置文件"+ff.getName()+"读取失败："+ex.getMessage());
			}
		}
	}

	@Override
	public List<String> getValue() {
		return global.getValue();
	}
}
