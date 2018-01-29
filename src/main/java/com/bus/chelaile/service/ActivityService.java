package com.bus.chelaile.service;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.mvc.QuestionParam;

public class ActivityService {

	public Object getRoomInfo(QuestionParam param) {
		
		JSONObject json = new JSONObject();
		json.put("mcName", "小车君");
		json.put("profilePhoto", "");
		json.put("activityId", "1");
		json.put("isLive", "1");
		json.put("money", "100000");
		json.put("relive", 2);
		json.put("onlineNum", "1231231");
		json.put("inviteCode", "123456A");
		
		return json;
	}
	
	
	
	
	
}
