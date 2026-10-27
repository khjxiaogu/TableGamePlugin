/**
 * Mirai Tablegames Plugin
 * Copyright (C) 2021  khjxiaogu
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.data.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.utils.FileUtil;

/**
 * 通过 HTTP GET 从外部接口获取一个 CDK 字符串。
 */
public final class CdkApi {
	private static final int MAX_RESPONSE_LENGTH = 8192;
	private static final String[] CDK_FIELDS = { "cdk", "code", "cdkey", "兑换码" };

	private CdkApi() {
	}

	/**
	 * 请求外部接口并返回 CDK 字符串。
	 * 
	 * @param uriTemplate 带 {placeholder} 的 URI 模板
	 * @param params      占位符参数，值会被 URL 编码
	 * @param headers     额外请求头，可为 null
	 * @param timeout     连接与读取超时（毫秒）
	 */
	public static String request(String uriTemplate, Map<String, String> params, Map<String, String> headers,
			int timeout) throws IOException {
		if (uriTemplate == null || uriTemplate.trim().isEmpty())
			throw new IOException("CDK接口地址未配置");
		String address = expand(uriTemplate.trim(), params);
		HttpURLConnection connection = null;
		try {
			URL url = new URL(address);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setConnectTimeout(timeout);
			connection.setReadTimeout(timeout);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Accept", "text/plain, application/json");
			connection.setRequestProperty("User-Agent", "TableGames-CDK/1.0");
			if (headers != null) {
				for (Entry<String, String> header : headers.entrySet()) {
					if (header.getKey() != null && header.getValue() != null)
						connection.setRequestProperty(header.getKey(), header.getValue());
				}
			}
			int code = connection.getResponseCode();
			InputStream input = code >= 200 && code < 300 ? connection.getInputStream()
					: connection.getErrorStream();
			String body = "";
			if (input != null) {
				try {
					body = new String(FileUtil.readAll(input), getCharset(connection));
				} finally {
					input.close();
				}
			}
			body = body.trim();
			if (code < 200 || code >= 300)
				throw new IOException("CDK接口返回HTTP " + code + (body.isEmpty() ? "" : "：" + brief(body)));
			return extractCdk(body);
		} finally {
			if (connection != null)
				connection.disconnect();
		}
	}

	private static String expand(String template, Map<String, String> params) throws UnsupportedEncodingException {
		String result = template;
		if (params != null) {
			for (Entry<String, String> param : params.entrySet()) {
				String value = param.getValue() == null ? "" : param.getValue();
				result = result.replace("{" + param.getKey() + "}", URLEncoder.encode(value, "UTF-8"));
			}
		}
		return result;
	}

	private static Charset getCharset(HttpURLConnection connection) {
		String contentType = connection.getContentType();
		if (contentType != null) {
			for (String part : contentType.split(";")) {
				String item = part.trim();
				if (item.toLowerCase(Locale.ROOT).startsWith("charset=")) {
					String name = item.substring("charset=".length()).trim().replace("\"", "");
					try {
						return Charset.forName(name);
					} catch (Exception ignored) {
					}
				}
			}
		}
		return StandardCharsets.UTF_8;
	}

	private static String extractCdk(String body) throws IOException {
		if (body == null)
			throw new IOException("CDK接口未返回内容");
		String result = body.trim();
		if (result.isEmpty())
			throw new IOException("CDK接口未返回内容");
		if (result.length() > MAX_RESPONSE_LENGTH)
			result = result.substring(0, MAX_RESPONSE_LENGTH).trim();
		// 兼容返回 {"cdk":"xxx"} 这类 JSON 的接口，非 JSON 时直接返回原文
		if (result.startsWith("{")) {
			try {
				JsonObject object = JsonParser.parseString(result).getAsJsonObject();
				String cdk = findCdk(object);
				if (cdk != null && !cdk.trim().isEmpty())
					return cdk.trim();
			} catch (Exception ignored) {
			}
		}
		return result;
	}

	private static String findCdk(JsonObject object) {
		for (String field : CDK_FIELDS) {
			JsonElement value = object.get(field);
			if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString())
				return value.getAsString();
			if (value != null && value.isJsonPrimitive())
				return value.getAsString();
		}
		JsonElement data = object.get("data");
		if (data != null && data.isJsonObject())
			return findCdk(data.getAsJsonObject());
		return null;
	}

	private static String brief(String body) {
		String line = body.replace('\r', ' ').replace('\n', ' ').trim();
		return line.length() > 200 ? line.substring(0, 200) + "..." : line;
	}
}
