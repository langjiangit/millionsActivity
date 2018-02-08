package com.bus.chelaile.common;

import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.config.PropertiesUtils;

public class Constants {
//    public static final int STATUS_SUCC = 0;
//    public static final int STATUS_FAIL = -1;
//    public static final int INT_UNDEFINED = -1;
    
    public static final String STATUS_REQUEST_SUCCESS = "00";
    public static final String STATUS_INTERNAL_ERROR = "02";
    public static final String STATUS_FUNCTION_NOT_ENABLED = "03";
    public static final String STATUS_NO_DATA = "00";
    public static final String STATUS_PARAM_ERROR = "05";
    
    
    public static final int LONGEST_CACHE_TIME = 30 * 24 * 60 * 60 - 1; 
    public static final int ONE_DAY_TIME = 1 * 24 * 60 * 60; //一天，单位 S
    public static final int SEVEN_DAY_TIME = 7 * 24 * 60 * 60; //七天，单位 S
    
    public static final int ONE_DAY_NEW_USER_PERIOD = 1 * 24 * 60 * 60 * 1000;  //一天，单位毫秒，默认新用户的时期。
    public static final int DEFAULT_NEW_USER_PERIOD = 7 * 24 * 60 * 60 * 1000;  //七天，单位毫秒，默认新用户的时期。
    
    public static final boolean ISTEST = Boolean.parseBoolean(PropertiesUtils.getValue(
			PropertiesName.PUBLIC.getValue(), "isTest", "false"));
    
    public static final boolean IS_SEDN_MACHINE = Boolean.parseBoolean(PropertiesUtils.getValue(
			PropertiesName.PUBLIC.getValue(), "isSendMachine", "false"));
    
    
    public static final String IOSNAME = "ios";
    
    public static final String ANDROIDNAME = "android";
    
    public static final String EMPTY_STR = "";
    
    // 版本控制号
    public static final int PLATFORM_LOG_ANDROID_0118 = 96;
	public static final int PLATFORM_LOG_IOS_0117 = 10480;
}
