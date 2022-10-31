package com.khjxiaogu.TableGames.platform.simplerobot.kookobjects;

import java.util.UUID;

public class SendMessageRequest {
	public int type=1;
	public String target_id;
	public String content;
	public String quote;
	public String nonce=UUID.randomUUID().toString();
	public String temp_target_id;
	public String chat_code;
	public SendMessageRequest(String target_id, String content) {
		super();
		this.type = type;
		this.target_id = target_id;
		this.content = content;
	}
	public SendMessageRequest(int type, String target_id, String content) {
		super();
		this.type = type;
		this.target_id = target_id;
		this.content = content;
	}


}
