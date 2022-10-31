package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class UpdateRoleRequest {
	public String channel_id;// body string true 频道 id, 如果频道是分组的 id,会同步给所有 sync=1 的子频道
	public String type="user_id";// body string false value 的类型，只能为"role_id","user_id",不传则默认为"user_id"
	public String value;// body string false 根据 type 的值，为用户 id 或角色 id
	public int allow;// body integer false 默认为 0,想要设置的允许的权限值
	public int deny;//body integer false 默认为 0,想要设置的拒绝的权限值
	public UpdateRoleRequest(String channel_id, String value, int allow, int deny) {
		super();
		this.channel_id = channel_id;
		this.value = value;
		this.allow = allow;
		this.deny = deny;
	}
	public UpdateRoleRequest(String channel_id, String type, String value, int allow, int deny) {
		super();
		this.channel_id = channel_id;
		this.type = type;
		this.value = value;
		this.allow = allow;
		this.deny = deny;
	}


}
