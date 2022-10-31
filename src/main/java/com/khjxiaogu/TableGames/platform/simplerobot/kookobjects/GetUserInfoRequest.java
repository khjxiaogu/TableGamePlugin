package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class GetUserInfoRequest {
	public String user_id;
	public String guild_id;
	public GetUserInfoRequest(String user_id, String guild_id) {
		super();
		this.user_id = user_id;
		this.guild_id = guild_id;
	}
	public GetUserInfoRequest(String user_id) {
		super();
		this.user_id = user_id;
	}
	

}
