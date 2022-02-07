/**
 * Mirai Song Plugin
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;

public class UserIdentifierSerializer {
	private static List<Function<String,Optional<UserIdentifier>>> serializers=Collections.synchronizedList(new ArrayList<>()); 
	public UserIdentifierSerializer() {
	}
	public static void addRawSerializer(Function<String,UserIdentifier> srl) {
		addSerializer(e->Optional.ofNullable(srl.apply(e)));
	}
	public static void addSerializer(Function<String,Optional<UserIdentifier>> srl) {
		serializers.add(e->{
			try {
				return srl.apply(e);
			}catch(Throwable t) {
				return Optional.empty();
			}
		});
	}
	public static UserIdentifier read(String s) {
		return serializers.stream().map(f->f.apply(s)).filter(Optional::isPresent).findFirst().orElseGet(Optional::empty).orElseThrow(NoSuchElementException::new);
	}
	public static String write(UserIdentifier i) {
		return i.serialize();
	}

}
