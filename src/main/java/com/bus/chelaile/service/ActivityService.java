package com.bus.chelaile.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.mvc.model.ActivityInfo;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.config.PropertiesUtils;

public class ActivityService {

	Logger logger = LoggerFactory.getLogger(ActivityService.class);
	private static final int MAX_ONLINE = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"max.online.num", "510000"));
	private static boolean HASCHANGEDNUM = false;
	
	/**
	 * 初始化获取房间信息
	 * 
	 * @param param
	 * @return
	 */
	public Object getRoomInfo(QuestionParam param, Answer_activity activity, int totalLive) {
		// 校验总人数
//		totalLive = check(totalLive);
		
		JSONObject json = new JSONObject();
		json.put("mcName", "小车君");
		json.put("profilePhoto", "https://image3.chelaile.net.cn/748355e296cd4f4d814f6b4e80207799");

		json.put("activityId", activity.getActivityId());
		json.put("isLive", activity.getIsonLive());
		json.put("money", activity.getTotalBonus());
		json.put("onlineNum", totalLive);
		
		return json;
	}
	

	/*
	 * 出现过一次负数，就启动兜底策略
	 */
//	private int check(int totalLive) {
//		if (totalLive >= 0 && !HASCHANGEDNUM) {
//			return totalLive;
//		} else {
//			HASCHANGEDNUM = true; // 修改过一次之后，后面保持这个数
//			logger.info("人数为负，需要调整……  ");
//			return (int) (MAX_ONLINE + (2000 * Math.random()));
//		}
//	}


	/**
	 * 获取活动信息
	 * 
	 * @param questionParam
	 * @return
	 */
	public ActivityInfo getActivitInfoFromHome(QuestionParam questionParam) {
		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			ActivityInfo returnActivity = new ActivityInfo();
			returnActivity.setH2("");
			returnActivity.setDate("敬请期待");
			returnActivity.setTime("神秘大奖");
			returnActivity.setTotalMoney("???0000");
//			if (StringUtils.isNoneBlank(questionParam.getAccountId())) {
//				AccountInfo accInfo = QuestionCache.getAccountInfo(questionParam.getAccountId());
//				returnActivity.setRelive(accInfo.getCardNum());
//				returnActivity.setInviteCode(accInfo.getInviteCode());
//				returnActivity.setCanFillCode(accInfo.getCanFillCode());
//			}
			return returnActivity;
		}

		ActivityInfo returnActivity = new ActivityInfo();
		String[] time = activity.getStartTime().split(" ");
		returnActivity.setH2(activity.getActivityName());
		returnActivity.setDate(DateUtil.getDescDate(time[0]));
		returnActivity.setTime(time[1].split(":")[0] + ":" + time[1].split(":")[1]);
		returnActivity.setTotalMoney(activity.getTotalBonus() + "");
		returnActivity.setIsLive(activity.getIsonLive());

//		if (StringUtils.isNoneBlank(questionParam.getAccountId())) {
//			AccountInfo accInfo = QuestionCache.getAccountInfo(questionParam.getAccountId());
//			returnActivity.setRelive(accInfo.getCardNum());
//			returnActivity.setInviteCode(accInfo.getInviteCode());
//			returnActivity.setCanFillCode(accInfo.getCanFillCode());
//		}
		return returnActivity;
	}

	public static void main(String[] args) {
	}

}
