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
