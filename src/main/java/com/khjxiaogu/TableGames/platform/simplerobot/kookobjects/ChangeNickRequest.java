package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class ChangeNickRequest {
	public String guild_id;
	public String nickname;
	public String user_id;
	public ChangeNickRequest(String guild_id, String nickname, String user_id) {
		super();
		this.guild_id = guild_id;
		this.nickname = nickname;
		this.user_id = user_id;
	}
	

}
