package com.khjxiaogu.TableGames.platform.simplerobot;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.ChangeNickRequest;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.CreateRoleRequest;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.GetUserInfoRequest;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.GetUserInfoResponse;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.KookAPIException;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.SendFileResponse;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.SendMessageRequest;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.SendMessageResponse;
import com.khjxiaogu.TableGames.platform.simplerobot.kookobjects.UpdateRoleRequest;
import com.khjxiaogu.TableGames.utils.Utils;

public class KookAPI {
	Gson gs = new GsonBuilder().create();

	String baseUrl = "https://www.kookapp.cn";
	private String token;

	public KookAPI(String tok) {
		token = tok;
		
	}

	private void setHeader(HttpRequestBase req) {
		req.addHeader("Authorization", "Bot " + token);
	}

	private HttpEntity jsonEntity(Object json) {
		return new StringEntity(gs.toJson(json), ContentType.APPLICATION_JSON);
	}

	private <T> T getResponse(HttpRequestBase request,Class<T> type) {
		
		try(CloseableHttpClient httpClient = HttpClientBuilder.create().useSystemProperties().setDefaultRequestConfig(RequestConfig.custom().build()).build()){
			
			try(CloseableHttpResponse response = httpClient.execute(request)){
				HttpEntity responseEntity = response.getEntity();
				JsonObject jo = JsonParser
						.parseString(new String(Utils.readAll(responseEntity.getContent()), StandardCharsets.UTF_8))
						.getAsJsonObject();
				int code = jo.get("code").getAsInt();
				String message = jo.get("message").getAsString();
				if (code != 0) {
					throw new KookAPIException(code, message);
				}
				if(type!=null)
					return gs.fromJson(jo.get("data"), type);
			}
			return null;
		} catch (Exception e) {
			if(e instanceof KookAPIException)
				throw (KookAPIException)e;
			throw new KookAPIException("获取API错误", e);
		}
	}
	public <T> T jsonPost(String uri,Object request,Class<T> responsetype) {
		HttpPost post = new HttpPost(baseUrl + uri);
		setHeader(post);
		post.setEntity(jsonEntity(request));
		return getResponse(post,responsetype);
	}
	public <T> T get(String uri,Object request,Class<T> responsetype) {
		StringBuilder urib=new StringBuilder(baseUrl);
		urib.append(uri);
		if(request!=null) {
			urib.append("?");
			Field[] fs=request.getClass().getFields();
			boolean isFirst=true;
			for(Field f:fs) {
				Object val=null;
				try {
					val = f.get(request);
				} catch (IllegalArgumentException|IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(val!=null) {
					if(isFirst)
						isFirst=false;
					else
						urib.append("&");
					urib.append(f.getName()).append("=").append(String.valueOf(val));
					
				}
			}
		}
		HttpGet post = new HttpGet(urib.toString());
		setHeader(post);
		return getResponse(post,responsetype);
	}

	/*public String sendText(String id, String msg) {
		return jsonPost("/api/v3/message/create",new SendMessageRequest(id, msg),SendMessageResponse.class).msg_id;
	}

	public String sendMarkdown(String id, String msg) {
		return jsonPost("/api/v3/message/create",new SendMessageRequest(9,id, msg),SendMessageResponse.class).msg_id;
	}
	public String sendImage(String id, String uri) {
		return jsonPost("/api/v3/message/create",new SendMessageRequest(2,id, uri),SendMessageResponse.class).msg_id;
	}
	public String sendPrivateText(String id, String msg) {
		return jsonPost("/api/v3/direct-message/create",new SendMessageRequest(id, msg),SendMessageResponse.class).msg_id;
	}

	public String sendPrivateMarkdown(String id, String msg) {
		return jsonPost("/api/v3/direct-message/create",new SendMessageRequest(9,id, msg),SendMessageResponse.class).msg_id;
	}
	public String sendPrivateImage(String id, String uri) {
		return jsonPost("/api/v3/direct-message/create",new SendMessageRequest(2,id, uri),SendMessageResponse.class).msg_id;
	}*/
	/*public String sendFile(byte[] file) {
		HttpPost post = new HttpPost(baseUrl + "/api/v3/asset/create");
		setHeader(post);
		post.setEntity(MultipartEntityBuilder.create().addBinaryBody("file", file,ContentType.IMAGE_JPEG,"ChatImage.jpg").build());
		return getResponse(post,SendFileResponse.class).url;
	}*/
	public void setNick(String gid,String uid,String nick) {
		jsonPost("/api/v3/guild/nickname",new ChangeNickRequest(gid,nick, uid),null);
	}
	/*public String getNick(String gid,String uid) {
		return get("/api/v3/user/view",new GetUserInfoRequest(uid,gid),GetUserInfoResponse.class).nickname;
	}*/
	/*public String getName(String uid) {
		return get("/api/v3/user/view",new GetUserInfoRequest(uid),GetUserInfoResponse.class).username;
	}*/
	public void createUserRole(String cid,String uid) {
		jsonPost("/api/v3/channel-role/create",new CreateRoleRequest(cid, uid),null);
	}
	public void deleteUserRole(String cid,String uid) {
		jsonPost("/api/v3/channel-role/delete",new CreateRoleRequest(cid, uid),null);
	}
	public void createRole(String cid) {
		jsonPost("/api/v3/channel-role/create",new CreateRoleRequest(cid,"role_id", "0"),null);
	}
	public void deleteRole(String cid) {
		jsonPost("/api/v3/channel-role/delete",new CreateRoleRequest(cid,"role_id", "0"),null);
	}
	public void setMute(String cid,String uid) {
		try {
			createUserRole(cid,uid);
		}catch(KookAPIException ex) {
			
		}
		jsonPost("/api/v3/channel-role/update",new UpdateRoleRequest(cid, uid,0,4096|16384),null);
	}
	public void setUnmute(String cid,String uid) {
		deleteUserRole(cid,uid);
	}
	public void setAllMute(String cid) {
		try {
			createRole(cid);
		}catch(KookAPIException ex) {
			
		}
		jsonPost("/api/v3/channel-role/update",new UpdateRoleRequest(cid,"role_id","0",0,4096|16384),null);
	}
	/*public boolean isAdmin(String gid,String uid) {
		return Arrays.stream(get("/api/v3/user/view",new GetUserInfoRequest(uid,gid),GetUserInfoResponse.class).roles).anyMatch(t->t==5434834);
	}*/
	public void setAllUnmute(String cid) {
		deleteRole(cid);
	}
}
