package com.khjxiaogu.TableGames.platform;

public interface RoomMessageEvent {
	AbstractUser getSender();
	AbstractRoom getRoom();
}
