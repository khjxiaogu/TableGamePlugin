package com.khjxiaogu.TableGames.platform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import love.forte.simbot.ID;
import love.forte.simbot.bot.OriginBotManager;

public class SBId implements UserIdentifier {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String idx;
	private static final Map<String,SBId> cache=new HashMap<>();
	public SBId(String id) {
		super();
		idx=id;
	}
	public SBId(ID id) {
		super();
		idx=id.toString();
	}
	@Override
	public String getId() {
		return getIdX().toString();
	}

	@Override
	public String serialize() {
		return "sb:"+getId();
	}
	public static SBId of(String id) {
		return cache.computeIfAbsent(id,SBId::new);
	}
	public static SBId of(ID id) {
		return cache.computeIfAbsent(id.toString(),t->new SBId(id));
	}
	public static SBId load(String id) {
		if(id.startsWith("sb:")) {
			return of(id.substring(3));
		}
		return null;
	}
	@Override
	public boolean isActual() {
		return true;
	}
	public ID getIdX() {
		return ID.$(idx);
	}

}
