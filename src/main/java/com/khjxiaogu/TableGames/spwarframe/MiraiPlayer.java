package com.khjxiaogu.TableGames.spwarframe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.khjxiaogu.TableGames.platform.AbstractPlayer;
import com.khjxiaogu.TableGames.utils.MessageListener.MsgType;
import com.khjxiaogu.TableGames.utils.Utils;

public class MiraiPlayer implements Player {
	AbstractPlayer intern;
	public MiraiPlayer(AbstractPlayer mem) {
		intern=mem;
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
		intern.registerListener((msg,msgtype)->{
			if(msgtype==MsgType.PRIVATE) {
				msgc.accept(Arrays.asList(Utils.getPlainText(msg).split(" ")));
			}
		});
	}
	@Override
	public void removeListener() {
		intern.releaseListener();
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
