package com.khjxiaogu.TableGames.game.spwarframe;

import java.util.List;
import java.util.function.Consumer;

public interface Player {

	String getName();

	void sendMessage(String msg);

	void listenMessage(Consumer<List<String>> msgc);

	void removeListener();

	void makeSpeak();

	void makeMute();
	void setNumber(int num);
	void removeNumber();
}