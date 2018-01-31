package com.bus.chelaile.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.account.AccountActivityStatus;
import com.bus.chelaile.model.account.AccountInfo;
import com.bus.chelaile.service.StaticService;

public class QuestionCache {

	protected static final Logger logger = LoggerFactory.getLogger(QuestionCache.class);
	
	private static final String CODE_CREATE_KEY = "CODE_CREATE_KEY";

	
	// 目前活动状态的记录
	public static ActivityStatus getQuestionStatus(int activityId) {
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
		
		if(questionStatus.getQuestionN() == 0) {
			Object realiveO = CacheUtil.getPubClientNumber();
			int realLive = 0;
			if(realiveO != null) {
				realLive = Integer.parseInt((String)CacheUtil.getPubClientNumber());
			}
			
			logger.info("获取到的实际链接数是：realLive={}", realLive);
			StaticService.updateAnswerDataFromLivePeople(realLive, activityId, true);
		}
	}


	public static void updateQuestionStatus(int activityId, ActivityStatus questionStatus) {
		
		String questionStatusKey = "QUESTIONSTATUS#" + activityId;
		CacheUtil.setToRedis(questionStatusKey, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(questionStatus));
		
	}


	// 收集答案进队列， 供后续消费分析
	public static String getAnswerLogListKey(int subjectId) {
		return new StringBuilder("QUESTION_ANSWERLOG#").append(subjectId).toString();
	}
	
	public static String getAnswerLogListField(String accountId) {
		return new StringBuilder("QUESTION_ANSWERLOG#ACCOUNT#").append(accountId).toString();
	}
	

	 // 存储邀请码对应用户的关系
	public static String getCodeCacheKey(String code) {
		return new StringBuilder("QUESTION#INVITECODE#").append(code).toString();
	}
	
	// 存储每道题结束后答对的人
	public static String getLivePeoPleKey(int activityId, int questionN) {
		return new StringBuilder("QUESTION_LIVEPEOPLE#").append(activityId).append("#").append(questionN).toString();
	}
	
	// 记录用户当前活动的状态
	// 包括：生死、答至第几题、可否继续使用复活卡
	public static AccountActivityStatus getAccountStatus(String accountId, int activityId) {
		String key = "QUESTION_ACCOUNTSTATUS#" + activityId + "#" + accountId;
		String value = (String) CacheUtil.getFromRedis(key);
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
	public static void updateAccountStatus(String accountId, AccountActivityStatus accountStatu, int activityId) {
		String key = "QUESTION_ACCOUNTSTATUS#" + activityId + "#" + accountId;
		CacheUtil.setToRedis(key, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(accountStatu));
		
		System.out.println("更新用户的答题状态, accountId=" + accountId + ", accountStatus=" + JSONObject.toJSONString(accountStatu));
	}
	
	// 获取用户信息
	// 包括：复活卡数目
	public static AccountInfo getAccountInfo(String aid) {
		String key = "QUESTION_ACCOUNTINFO#" + aid;
		String value = (String) CacheUtil.getFromRedis(key);
		if(StringUtils.isNoneBlank(value)) {
			return JSONObject.parseObject(value, AccountInfo.class);
		} else {
			logger.info("新用户第一次参加活动！ , accountId={}", aid);
			AccountInfo info = new AccountInfo(aid);
			updateAccountInfo(aid, info);
			return info;
		}
	}

	public static void updateAccountInfo(String aid, AccountInfo aInfo) {
		String key = "QUESTION_ACCOUNTINFO#" + aid;
		CacheUtil.setToRedis(key, Constants.LONGEST_CACHE_TIME, JSONObject.toJSONString(aInfo));
	}
	
	
	/**
	 * 
	 * @param aid
	 *            用户id
	 * @param isCanUsedCard
	 *            能否使用复活卡
	 * @return
	 */
	public static boolean useCard(String aid, boolean isCanUsedCard, boolean isUpdate) {
		AccountInfo aInfo = QuestionCache.getAccountInfo(aid);
		if (!isCanUsedCard) {
			logger.info("不能够使用复活卡, accountId={}", aid);
			return false;
		}
		if (aInfo != null && aInfo.getCardNum() > 0) {
			aInfo.reduceOne();
			if(isUpdate)
				QuestionCache.updateAccountInfo(aid, aInfo);
			logger.info("使用复活卡重生, accountId={}", aid);
			return true;
		}
		return false;
	}


	public static String getCodeCreateKey() {
		return CODE_CREATE_KEY;
	}
}
