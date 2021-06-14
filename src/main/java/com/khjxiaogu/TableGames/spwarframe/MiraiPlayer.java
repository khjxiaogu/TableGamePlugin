package com.khjxiaogu.TableGames.spwarframe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.TableGames.AbstractPlayer;
import com.khjxiaogu.TableGames.HumanPlayer;
import com.khjxiaogu.TableGames.utils.ListenerUtils;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

import net.mamoe.mirai.contact.Member;

public class MiraiPlayer implements Player {
	AbstractPlayer intern;
	public MiraiPlayer(Member mem) {
		intern=new HumanPlayer(mem);
	}
	@Override
	public String getName() {
		return intern.getNameCard();
	}
	@Override
	public void sendMessage(String msg) {
		intern.sendPrivate(msg);
	}
	@Override
	public void listenMessage(Consumer<List<String>> msgc) {
		ListenerUtils.registerListener(intern.getId(),(msg,msgtype)->{
			if(msgtype==MsgType.PRIVATE) {
				msgc.accept(Arrays.asList(Utils.getPlainText(msg).split(" ")));
			}
		});
	}
	@Override
	public void removeListener() {
		ListenerUtils.releaseListener(intern.getId());
	}
	@Override
	public void makeSpeak() {
		intern.tryUnmute();
	}
	@Override
	public void makeMute() {
		intern.tryMute();
	}
	@Override
	public void setNumber(int num) {
		String nc=intern.getNameCard();
		if (nc.indexOf('|') != -1) {
			nc = nc.split("\\|")[1];
		}
		intern.setNameCard(num+ "号 |" + nc);
	}

	@Override
	public void removeNumber() {
		String nc = intern.getNameCard();
		if (nc.indexOf('|') != -1) {
			nc = nc.split("\\|")[1];
		}
		intern.setNameCard(nc);
	}

}
