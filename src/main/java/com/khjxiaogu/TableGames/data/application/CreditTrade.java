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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.khjxiaogu.TableGames.platform.GlobalMain;
import com.khjxiaogu.TableGames.platform.UserIdentifier;
import com.khjxiaogu.TableGames.utils.FileUtil;

public class CreditTrade {
	int pt;
	String itm;
	String dn;
	int cnt;
	/** 非空时表示这是一个CDK商品，购买后请求该地址获取CDK。 */
	String cdkUri;
	Map<String, String> cdkHeaders = Collections.emptyMap();
	int cdkTimeout = 10000;

	public static ArrayList<CreditTrade> trades = new ArrayList<>();
	static {
		new CreditTrade(3, "狼人杀vip券", 1);
		new CreditTrade(10, "狼人杀vip券", 4);
		new CreditTrade(20, "狼人杀vip券", 9);
		new CreditTrade(30, "狼人杀vip券", 15);
		new CreditTrade(5, "AI生图券", 1);
		new CreditTrade(8, "100k配额券", 1);
	}

	private static final String CDK_CONFIG_TEMPLATE = "{\n" + "  \"trades\": [\n" + "    {\n"
			+ "      \"name\": \"示例CDK商品\",\n" + "      \"item\": \"example_cdk\",\n" + "      \"price\": 10,\n"
			+ "      \"count\": 1,\n" + "      \"enabled\": false,\n"
			+ "      \"uri\": \"http://127.0.0.1:8080/cdk?player={player}&item={item}&count={count}&order={order}\",\n"
			+ "      \"timeout\": 10000,\n" + "      \"headers\": {\"Authorization\": \"Bearer your-token\"}\n" + "    }\n"
			+ "  ]\n" + "}\n";

	public CreditTrade(int pt, String itm, int cnt) {
		this(pt, itm, itm, cnt, null, null, 10000);
		add();
	}

	public CreditTrade(int pt, String itm, String dn, int cnt) {
		this(pt, itm, dn, cnt, null, null, 10000);
		add();
	}

	private CreditTrade(int pt, String itm, String dn, int cnt, String cdkUri, Map<String, String> cdkHeaders,
			int cdkTimeout) {
		this.pt = pt;
		this.itm = itm;
		this.dn = dn;
		this.cnt = cnt;
		this.cdkUri = cdkUri;
		if (cdkHeaders != null)
			this.cdkHeaders = cdkHeaders;
		if (cdkTimeout > 0)
			this.cdkTimeout = cdkTimeout;
	}

	private void add() {
		CreditTrade.trades.add(this);
	}

	/**
	 * 读取数据目录下的 cdk.json，加载通过外部接口获取CDK的商品。
	 * 文件不存在时会生成一份带示例的模板。
	 */
	public static synchronized void loadCdkTrades(File dataFolder) {
		File config = new File(dataFolder, "cdk.json");
		if (!config.exists()) {
			try {
				FileUtil.transfer(CDK_CONFIG_TEMPLATE, config);
				GlobalMain.getLogger().info("已生成CDK商城配置模板：" + config.getAbsolutePath());
			} catch (IOException e) {
				GlobalMain.getLogger().error(e);
				GlobalMain.getLogger().warning("生成CDK商城配置模板失败，CDK商品不可用");
			}
			return;
		}
		JsonArray array = null;
		try {
			JsonElement root = JsonParser.parseString(FileUtil.readString(config));
			if (root.isJsonArray()) {
				array = root.getAsJsonArray();
			} else if (root.isJsonObject() && root.getAsJsonObject().has("trades")
					&& root.getAsJsonObject().get("trades").isJsonArray()) {
				array = root.getAsJsonObject().getAsJsonArray("trades");
			}
		} catch (Exception e) {
			GlobalMain.getLogger().error(e);
			GlobalMain.getLogger().warning("CDK商城配置读取失败，CDK商品不可用");
			return;
		}
		if (array == null) {
			GlobalMain.getLogger().warning("CDK商城配置格式错误，应为数组或包含trades数组的对象");
			return;
		}
		int loaded = 0;
		for (JsonElement element : array) {
			try {
				if (!element.isJsonObject())
					throw new IllegalArgumentException("商品配置必须是对象");
				CreditTrade trade = fromJson(element.getAsJsonObject());
				if (trade != null) {
					trade.add();
					loaded++;
				}
			} catch (Exception e) {
				GlobalMain.getLogger().warning("CDK商品配置错误：" + e.getMessage());
			}
		}
		GlobalMain.getLogger().info("已加载 " + loaded + " 个CDK商品");
	}

	private static CreditTrade fromJson(JsonObject json) {
		if (json.has("enabled") && !json.get("enabled").getAsBoolean())
			return null;
		String name = getString(json, "name", getString(json, "display", null));
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("缺少name");
		String item = getString(json, "item", name);
		if (!json.has("price"))
			throw new IllegalArgumentException("缺少price");
		int price = json.get("price").getAsInt();
		if (price <= 0)
			throw new IllegalArgumentException("price必须大于0");
		int count = json.has("count") ? json.get("count").getAsInt() : 1;
		if (count <= 0)
			throw new IllegalArgumentException("count必须大于0");
		String uri = getString(json, "uri", null);
		if (uri == null || uri.trim().isEmpty())
			throw new IllegalArgumentException("缺少uri");
		Map<String, String> headers = new LinkedHashMap<>();
		if (json.has("headers") && json.get("headers").isJsonObject()) {
			for (Map.Entry<String, JsonElement> header : json.getAsJsonObject("headers").entrySet()) {
				if (header.getValue().isJsonPrimitive())
					headers.put(header.getKey(), header.getValue().getAsString());
			}
		}
		int timeout = json.has("timeout") ? json.get("timeout").getAsInt() : 10000;
		return new CreditTrade(price, item, name, count, uri.trim(), headers, timeout);
	}

	private static String getString(JsonObject json, String name, String def) {
		if (json.has(name) && json.get(name).isJsonPrimitive())
			return json.get(name).getAsString();
		return def;
	}

	public static class TradeResult {
		public final boolean success;
		public final String cdk;
		public final String message;

		private TradeResult(boolean success, String cdk, String message) {
			this.success = success;
			this.cdk = cdk;
			this.message = message;
		}

		public static TradeResult ok() {
			return new TradeResult(true, null, "购买成功");
		}

		public static TradeResult cdk(String cdk) {
			return new TradeResult(true, cdk, null);
		}

		public static TradeResult fail(String message) {
			return new TradeResult(false, null, message);
		}
	}

	/**
	 * 执行购买。CDK商品会先扣除积分，再请求外部接口；接口失败时自动退还积分。
	 */
	public TradeResult execute(UserIdentifier id) {
		PlayerCredit pcd = GlobalMain.credit.get(id);
		if (pcd.withdrawPT(pt) < 0)
			return TradeResult.fail("积分不足");

		if (cdkUri == null || cdkUri.isEmpty()) {
			pcd.giveItem(itm, cnt);
			return TradeResult.ok();
		}

		Map<String, String> params = new HashMap<>();
		params.put("uid", id.serialize());
		params.put("player", GlobalMain.bindings.getBinding(id).serialize());
		params.put("item", itm);
		params.put("name", dn);
		params.put("count", String.valueOf(cnt));
		params.put("order", UUID.randomUUID().toString());
		try {
			String cdk = CdkApi.request(cdkUri, params, cdkHeaders, cdkTimeout);
			return TradeResult.cdk(cdk);
		} catch (Exception e) {
			pcd.givePT(pt);
			GlobalMain.getLogger().warning("CDK获取失败，已退还 " + pt + " 积分：" + e.getMessage());
			return TradeResult.fail("CDK获取失败，积分已退还");
		}
	}

	public String getDisplay() {
		StringBuilder sb = new StringBuilder(dn);
		if (cnt > 1) {
			sb.append("x").append(cnt);
		}
		return sb.toString();
	}

	public boolean isCdk() {
		return cdkUri != null && !cdkUri.isEmpty();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getDisplay());
		if (isCdk()) {
			sb.append("（兑换码）");
		}
		sb.append(":").append(pt).append("积分");
		return sb.toString();
	}

	public static String getList() {
		StringBuilder ips = new StringBuilder();
		int i = 0;
		for (CreditTrade ct : CreditTrade.trades) {
			ips.append(++i).append("、").append(ct.toString()).append("\n");
		}
		ips.append("输入@我 购买 序号 来购买物品");
		return ips.toString();
	}
}
