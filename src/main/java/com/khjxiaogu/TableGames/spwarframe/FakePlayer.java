package com.khjxiaogu.TableGames.spwarframe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class FakePlayer implements Player {
	GameManager room;
	public FakePlayer(GameManager room) {
		this.room = room;
	}

	@Override
	public String getName() {
		return "无玩家";
	}

	@Override
	public void sendMessage(String msg) {
		System.out.println(msg);
	}

	@Override
	public void listenMessage(Consumer<List<String>> msgc) {
		((SpWarframe) room.p).getScheduler().executeLater(()->
		msgc.accept(Arrays.asList("跳过")),1000);
	}

	@Override
	public void removeListener() {
	}

	@Override
	public void makeSpeak() {
	}

	@Override
	public void makeMute() {
	}

	@Override
	public void setNumber(int num) {
	}

	@Override
	public void removeNumber() {
	}

}
