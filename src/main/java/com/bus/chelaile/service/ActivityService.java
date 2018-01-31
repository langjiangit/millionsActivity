package com.bus.chelaile.service;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.account.AccountInfo;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.mvc.model.ActivityInfo;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.New;

public class ActivityService {

	Logger logger = LoggerFactory.getLogger(ActivityService.class);

	/**
	 * 初始化获取房间信息
	 * 
	 * @param param
	 * @return
	 */
	public Object getRoomInfo(QuestionParam param, Answer_activity activity, int totaoLive) {

		JSONObject json = new JSONObject();
		json.put("mcName", "小车君");
		json.put("profilePhoto", "https://image3.chelaile.net.cn/6c712da75ff84f328630dce45e44a482");

		json.put("activityId", activity.getActivityId());
		// TODO 没有正在进行的活动，是否开放入口
		json.put("isLive", activity.getIsonLive());
		json.put("money", activity.getTotalBonus());
		json.put("onlineNum", totaoLive);
		
		AccountInfo accInfo = QuestionCache.getAccountInfo(param.getAccountId());
		json.put("relive", accInfo.getCardNum()); // 用户复活卡数量
		json.put("inviteCode", accInfo.getInviteCode());

		return json;
	}

	/**
	 * 获取活动信息
	 * 
	 * @param questionParam
	 * @return
	 */
	public ActivityInfo getActivitInfo(QuestionParam questionParam) {
		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			return null;
		}

		ActivityInfo returnActivity = new ActivityInfo();
		String[] time = activity.getStartTime().split(" ");
		returnActivity.setH2(activity.getActivityName());
		returnActivity.setDate(DateUtil.getDescDate(time[0]));
		returnActivity.setTime(time[1].split(":")[0] + ":" + time[1].split(":")[1]);
		returnActivity.setTotalMoney(activity.getTotalBonus() + "");
		returnActivity.setIsLive(activity.getIsonLive());

		if (StringUtils.isNoneBlank(questionParam.getAccountId())) {
			AccountInfo accInfo = QuestionCache.getAccountInfo(questionParam.getAccountId());
			returnActivity.setRelive(accInfo.getCardNum());
			returnActivity.setInviteCode(accInfo.getInviteCode());
			returnActivity.setCanFillCode(accInfo.getCanFillCode());
		}
		return returnActivity;
	}

	public static void main(String[] args) {
		List<Answer_activity> activities = New.arrayList();
		Answer_activity a = new Answer_activity();
		a.setStartTime("2018-01-27 11:11:00");
		Answer_activity b = new Answer_activity();
		b.setStartTime("2018-01-27 11:12:00");
		Answer_activity c = new Answer_activity();
		c.setStartTime("2018-01-27 10:12:00");

		// activities.add(a);
		// activities.add(b);
		// activities.add(c);

//		Collections.sort(activities, ACTIVITY_COMPARATOR);
		for (Answer_activity ac : activities) {
			System.out.println(ac.getStartTime());
		}

		// System.out.println(isbefore5min("2018-01-31 16:10:00"));
	}

}
