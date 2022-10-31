package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

public class GetUserInfoResponse {
	public String id;//	用户的 id
	public String username;//	string	用户的名称
	public String nickname;//	string	用户在当前服务器的昵称
	public String identify_num;//	string	用户名的认证数字，用户名正常为：user_name#identify_num
	public boolean online;//	当前是否在线
	public int status;//用户的状态, 0 和 1 代表正常，10 代表被封禁
	public String avatar;//	string	用户的头像的 url 地址
	public String vip_avatar;//	string	vip 用户的头像的 url 地址，可能为 gif 动图
	public boolean is_vip;//	boolean	是否为会员
	public boolean bot;//	boolean	是否为机器人
	public boolean mobile_verified;//	boolean	是否手机号已验证
	public int[] roles;//	Array	用户在当前服务器中的角色 id 组成的列表
	public int joined_at;//	int	加入服务器时间
	public int active_time;//	int	活跃时间
	public GetUserInfoResponse() {
	}

}
