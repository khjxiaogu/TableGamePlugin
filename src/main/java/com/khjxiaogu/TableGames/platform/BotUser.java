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
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.khjxiaogu.TableGames.platform;

public class BotUser implements BotUserLogic {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6179212605268629887L;
	private AbstractBotUser internal;
	public BotUser(AbstractBotUser internal) {
		this.internal = internal;
	}

	@Override
	public void onPublic(String msg) {
	}

	@Override
	public void onPrivate(String msg) {
	}

	public AbstractBotUser getPlayer() {
		return internal;
	}

}
