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
package com.khjxiaogu.TableGames.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    private static boolean canGetCalendar=true;
    private static boolean canGetDate=true;
    private static boolean canGetNano=true;
    public static long getTime() {
    	if(canGetCalendar) {
        	try {
        		return Calendar.getInstance().getTimeInMillis();
        	}catch(Throwable T){
        		T.printStackTrace();
        		canGetCalendar=false;
        	}
    	}else if(canGetDate) {
	    	try {
	    		return new Date().getTime();
	    	}catch(Throwable T) {
	    		T.printStackTrace();
	    		canGetDate=false;
	    	}
    	}else if(canGetNano)
        	try {
        		return System.nanoTime()/1000000L;
        	}catch(Throwable T) {
        		T.printStackTrace();
        		canGetNano=false;
        	}
    	return System.currentTimeMillis();
    }

}
