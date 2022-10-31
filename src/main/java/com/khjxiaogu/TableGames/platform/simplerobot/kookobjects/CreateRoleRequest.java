package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class CreateRoleRequest {
	public String channel_id;// body string true 频道 id, 如果频道是分组的 id,会同步给所有 sync=1 的子频道
	public String type="user_id";// body string false value 的类型，只能为"role_id","user_id",不传则默认为"user_id"
	public String value;// body string false 根据 type 的值，为用户 id 或角色 id
	public CreateRoleRequest(String channel_id, String value) {
		super();
		this.channel_id = channel_id;
		this.value = value;
	}
	public CreateRoleRequest(String channel_id, String type, String value) {
		super();
		this.channel_id = channel_id;
		this.type = type;
		this.value = value;
	}

}
