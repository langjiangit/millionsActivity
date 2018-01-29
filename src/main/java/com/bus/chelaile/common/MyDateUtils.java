package com.bus.chelaile.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDateUtils {

	   /** 
	    * 获取过去第几天的日期 
	    * 
	    * @param past 
	    * @return 
	    */  
	   public static String getFutureDate(int past) {  
	       Calendar calendar = Calendar.getInstance();  
	       calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);  
	       Date today = calendar.getTime();  
	       SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");  
	       String result = format.format(today);  
	       return result;  
	   }  
	
}
