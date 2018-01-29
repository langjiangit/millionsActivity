package com.bus.chelaile.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.account.AccountActivityStatus;
import com.bus.chelaile.model.account.AccountInfo;

public class QuestionCache {

	protected static final Logger logger = LoggerFactory.getLogger(QuestionCache.class);

	
	// 目前活动状态的记录
	public static ActivityStatus getQuestionStatusKey(int activityId) {
		String questionStatusKey = "QUESTIONSTATUS#" + activityId;
		String status = (String)CacheUtil.getFromRedis(questionStatusKey);
		if(null == status) {
			return new ActivityStatus();
		} else {
			return JSON.parseObject(status, ActivityStatus.class);
		}
	}


	// 发布题目，更新状态
	// 第一位 +1
	public static void addPubQuestion(ActivityStatus questionStatus, int activityId) {
		questionStatus.addNumber();
		questionStatus.setQuestionS(0);
		
		QuestionCache.updateQuestionStatus(activityId, questionStatus);
	}


	public static void updateQuestionStatus(int activityId, ActivityStatus questionStatus) {
		
		String questionStatusKey = "QUESTIONSTATUS#" + activityId;
		CacheUtil.setToRedis(questionStatusKey, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(questionStatus));
		
	}


	// 收集答案进队列， 供后续消费分析
	public static String getAnswerLogListKey(int subjectId) {
		return new StringBuilder("QUESTION_ANSWERLOG#").append(subjectId).toString();
	}
	
	
	// 记录用户当前活动的状态
	// 包括：生死、答至第几题、可否继续使用复活卡
	public static AccountActivityStatus getAccountStatus(String accountId) {
		String key = "QUESTION_ACCOUNTSTATUS" + accountId;
		String value = (String) CacheUtil.get(key);
		if(StringUtils.isNoneBlank(value)) {
			System.out.println("查询到用户状态, accountStatus=" + value);
			return JSONObject.parseObject(value, AccountActivityStatus.class);
		} else {
			System.out.println("查询不到用户状态, accountStatus 为空");
			return null;
		}
	}

	
	// 记录用户当前活动的状态
	// 包括：生死、答至第几题、可否继续使用复活卡
	public static void updateAccountStatus(String accountId, AccountActivityStatus accountStatu) {
		String key = "QUESTION_ACCOUNTSTATUS" + accountId;
		CacheUtil.set(key, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(accountStatu));
		
		System.out.println("更新用户的答题状态, accountId=" + accountId + ", accountStatus=" + JSONObject.toJSONString(accountStatu));
	}
	
	// 获取用户信息
	// 包括：复活卡数目
	public static AccountInfo getAccountInfo(String aid) {
		String key = "QUESTION_ACCOUNTINFO" + aid;
		String value = (String) CacheUtil.get(key);
		if(StringUtils.isNoneBlank(value)) {
			return JSONObject.parseObject(value, AccountInfo.class);
		} else {
			return null;
		}
	}

	
	public static void updateAccountInfo(String aid, AccountInfo aInfo) {
		String key = "QUESTION_ACCOUNTINFO" + aid;
		CacheUtil.set(key, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(aInfo));
	}
}
