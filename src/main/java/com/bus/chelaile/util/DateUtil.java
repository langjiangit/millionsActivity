package com.bus.chelaile.util;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;


public class DateUtil {

	
	//private static SimpleDateFormat simpleDateFormat_Long = new SimpleDateFormat("yyyyMMddHHmmss");
	
	/**
	 * 与当前时间做比较
	 * @param date	类型是 yyyyMMddHHmmss
	 * @return  返回 1 当前日期 > date, 返回 0  当前日期 = date, 返回 -1   当前日期 < date
	 * @throws ParseException 
	 */
	public static int compareNowDate(String date) throws ParseException {
//		String pattern = "yyyyMMddHHmmss";
		String pattern = "HH:mm";
		String nowDate = new SimpleDateFormat(pattern).format(new Date());
		return compareDate(nowDate, pattern, date, pattern);
	}
	
	/**
	 * 获取当前分钟
	 * @return
	 */
	public static String getMinuteStr() {
		String pattern = "HH:mm";
		String nowDate = new SimpleDateFormat(pattern).format(new Date());
		return nowDate;
	}
	
	/**
	 * 返回固定格式的时间
	 * @param date
	 * @param pattern
	 * @return
	 * @throws ParseException
	 */
	public static String getFormatTime(Date date, String pattern) throws ParseException {
		return new SimpleDateFormat(pattern).format(date);
	}
	
	
	/**
	 * 日期比较
	 * @param firstDate
	 * @param pattern
	 * @param firstDate
	 * @param lastpattern
	 * @return  1 firstDate > secondDate, 0 secondDate = firstDate, -1   firstDate < secondDate
	 * @throws ParseException 
	 */
	public static int compareDate(String firstDate, String pattern, String secondDate, String lastpattern) throws ParseException {
		Date date = new SimpleDateFormat(pattern).parse(firstDate);
		Date compareDate = new SimpleDateFormat(lastpattern).parse(secondDate);

		if (date.getTime() > compareDate.getTime()) {
			return 1;
		} else if (date.getTime() == compareDate.getTime()) {
			return 0;
		} else {
			return -1;
		}
	}

	public static String getTodayStr(String pattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(new Date());
	}
	
	/**
     * 获取信息流格式时间
     */
    public static String flowFormatTime(String timeStr) {
    	long time ;
    	try{
    		time = Long.parseLong(timeStr);
    	} catch(Exception e){
    		return "刚刚";
    	}
    	
        int seconds = Math.round((System.currentTimeMillis() - time) / 1000f);
        if (System.currentTimeMillis() < time - 60) {
            //发帖时间大于本地时间超过1min，直接显示“一天前”，抄微信设计
            return "1天前";
        } else if (seconds < 60) {
            return "刚刚";
        } else if (seconds < 60 * 60) {
            int min = Math.round(seconds / 60f);
            return min + "分钟前";
        } else {
            int hour = Math.round(seconds / 3600f);
            if (hour < 24) {
                return hour + "小时前";
            }
            return (hour / 24) + "天前";
        }
    }
    
    public static Date getDate(String timeStr, String pattern) {
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	try {
			return sdf.parse(timeStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	return new Date();
    }
}
