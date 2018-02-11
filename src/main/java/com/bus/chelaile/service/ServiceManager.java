package com.bus.chelaile.service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.AnswerData;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.model.account.AccountActivityStatus;
import com.bus.chelaile.model.account.AccountInfo;
import com.bus.chelaile.model.client.JsonStr;
import com.bus.chelaile.model.pubmsg.PubQuestion;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.mvc.model.ActivityInfo;
import com.bus.chelaile.thread.CalculateAnswerThread;
import com.bus.chelaile.thread.PushAnswerLogThread;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;

public class ServiceManager {
	@Autowired
	private PublishDataService questionService;
	@Autowired
	private PublishDataService publishDataService;
	@Autowired
	private ActivityService activityService;

	private static final int THREAD_COUNT = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"thread.count", "10"));
	private static final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"thread.count", "10")));

	protected static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

	static HashMap<Integer, String> rooms = new HashMap<Integer, String>();

	public String getClienSucMap(Object obj, String status) {
		JsonStr jsonStr = new JsonStr();
		jsonStr.setData(obj);
		jsonStr.setStatus(status);
		try {
			String json = JSON.toJSONString(jsonStr, SerializerFeature.BrowserCompatible);
			// JsonBinder.toJson(clientDto, JsonBinder.always);
			return json;
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			return "";
		}
	}

	public String getClientErrMap(String errmsg, String status) {
		JsonStr jsonStr = new JsonStr();
		jsonStr.setStatus(status);
		jsonStr.setErrmsg(errmsg);
		try {
			String json = JSON.toJSONString(jsonStr, SerializerFeature.BrowserCompatible);
			return json;
		} catch (Exception e1) {
			logger.error(e1.getMessage(), e1);
			return "";
		}
	}

	/**
	 * 发题 1、设置题目的开始答题和结束答题时间 2、push题目
	 * 
	 * @return
	 */
	public String SendQuestion(QuestionParam param) {

		int subjectId = param.getSubjectId();
		Answer_subject question = StaticService.getSubject(subjectId);
		if (question == null) {
			return getClientErrMap("发题失败，题目为空 , id=" + subjectId, Constants.STATUS_PARAM_ERROR);
		}
		if(StaticService.SENDED_SUBJECT.contains(subjectId)) {
			logger.error("重复发题！ ");
			return getClientErrMap("重复发题， id=" + subjectId, Constants.STATUS_PARAM_ERROR);
		}

		// 查询当前 答题活动状态
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(question.getActivityId());

		// pushlish 题目
		PubQuestion pubQuestion = new PubQuestion(questionStatus, question);
		try {
			System.out.println("发送的题目是" + JSONObject.toJSONString(pubQuestion) + ", 时间: "
					+ DateUtil.getFormatTime(new Date(), "yyyy-MM-dd HH:mm:ss"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Map<String, Object> sendQustion = New.hashMap();
		sendQustion.put("q", pubQuestion);
		sendQustion.put("s", System.currentTimeMillis());
		
		if (Constants.IS_SEDN_MACHINE) { // TODO  负责推送的机器
			Long pubResult = CacheUtil.publish(JSONObject.toJSONString(sendQustion));
			if (pubResult != 0) {
				logger.info("************发题成功, id={}", subjectId);
			} else {
				getClientErrMap("发题失败 , 题目是：" + question.getSubject(), Constants.STATUS_INTERNAL_ERROR);
			}
			QuestionCache.addPubQuestion(questionStatus, question.getActivityId());
			
//			// 所有试卷的map集合
			
			// 多线程阅卷
			int c=THREAD_COUNT;
			final CountDownLatch latch = new CountDownLatch(c);
			for(int i = 0;i < c; i ++) {
				 exec.schedule(new CalculateAnswerThread(subjectId, questionStatus, 
						 question.getActivityId(),i,latch),
					StaticService.EFFECTIVE_TIME, TimeUnit.MILLISECONDS);
			}
		}
		StaticService.setEffective(subjectId, questionStatus); // 设置题目生效时间和realOrder

		// 根据生成的题目有效截止时间，来启动执行‘消费答卷’的任务
		return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 答题 1、题目塞进队列 2、[10s]后 开始计算 3、保存计算结果到redis，供运营后台查询，并可修改
	 * 
	 * @return
	 */
	public String answerQuestion(QuestionParam param) {

		
		// 保证收题的速度，逻辑都去掉
		Answer_activity activity = StaticService.getNowOrNextActivity();
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());

//		int questionId = param.getSubjectId();
//		Answer_subject subject = StaticService.getSubject(questionId);
//		long startTime = subject.getEffectStartTime();
//		long endTime = subject.getEffectEndTime();
//		if (System.currentTimeMillis() > endTime || System.currentTimeMillis() < startTime) {
//			logger.info("提交答案超时， accountId={}, subjectId={}", param.getAccountId(), param.getSubjectId());
//			return getClientErrMap("已经超时啦", Constants.STATUS_FUNCTION_NOT_ENABLED);
//		}
		exec.execute(new PushAnswerLogThread(param, questionStatus.getQuestionN(), activity.getActivityId()));

		
		return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 运营后台，查询计算结果
	 * 
	 * @return
	 */
	public String queryQuestionData() {

		// 查询当前 答题活动状态
		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			logger.info("meiyou jingxing de huodong ");
			return getClienSucMap(new JSONObject(), "00");
		}
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());
		JSONObject responseJ = new JSONObject();
		AnswerData answerData = QuestionCache.getAnsweData(activity.getActivityId(), questionStatus.getQuestionN());
		responseJ.put("canChanged", 0);
		if (questionStatus.hasToCalculated()) {
			logger.info("开始计算， questionStatus={}", JSONObject.toJSONString(questionStatus));

			answerData.calcuRobots(activity.getActivityId(), questionStatus.getQuestionN(), activity.getRobotMultiple());
			// 更新当前态的数目, 供查询使用
//			QuestionCache.setAnsweData(activity.getActivityId(), questionStatus.getQuestionN(), answerData);
			
			// 设置下一个题目的初始答题数据
			answerData.copyFromLastData(activity.getActivityId(), questionStatus.getQuestionN());
//			QuestionCache.setAnsweData(activity.getActivityId(), questionStatus.getQuestionN() + 1, nextAnswerData);

			logger.info("计算结束 ");
			questionStatus.setQuestionS(2);
			QuestionCache.updateQuestionStatus(activity.getActivityId(), questionStatus);
		} else if (questionStatus.hasCalculated()) {
			answerData.copyFromLastData(activity.getActivityId(), questionStatus.getQuestionN());
			responseJ.put("canChanged", 1);
//			QuestionCache.setAnsweData(activity.getActivityId(), questionStatus.getQuestionN() + 1, nextAnswerData);
		} else if (!questionStatus.hasCalculated()) { // !=2 的情况，都不可以修改
			responseJ.put("canChanged", 0);
			// logger.info("当前状态不可修改数据：questionStatus={}",
			// JSONObject.toJSONString(questionStatus));
		}

		responseJ.put("index", questionStatus.getQuestionN() + 1);
		responseJ.put("answerDate", answerData);
		return getClienSucMap(responseJ, "00");
	}

	/**
	 * 运营后台，修改之前的计算结果
	 * 
	 * @return
	 */
	public String changeRobotNum(int index, int activityId, int watchingRobot, int option1Robot, int option2Robot,
			int option3Robot, int notAnsRobot, int reLivingRobot) {

		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			// logger.info("当前没有正在进行的活动");
			return getClienSucMap("", "00");
		}
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());
		int subjectId = StaticService.getSubjectId(questionStatus.getQuestionN());
		Answer_subject subject = StaticService.getSubject(subjectId);
		int rightAnswer = -1;
		if (subject != null) {
			rightAnswer = subject.getAnswer();
		}
		logger.info("调整数据、 当前修改题目，realOrder={}，subjectId={}, 正确答案={}", questionStatus.getQuestionN(), subjectId, rightAnswer);
		AnswerData answerData = QuestionCache.getAnsweData(activity.getActivityId(), questionStatus.getQuestionN());

		if (activityId != activity.getActivityId()) {
			logger.info("######## 调整数据异常, 请求参数与缓存不一致");
			logger.error("调整数据异常 ");
			return getClienSucMap("", "00");
		}

		answerData.changeData(rightAnswer, watchingRobot, option1Robot, option2Robot, option3Robot, notAnsRobot,
				reLivingRobot, activityId, questionStatus.getQuestionN());
//		QuestionCache.setAnsweData(activity.getActivityId(), questionStatus.getQuestionN(), answerData);
		return getClienSucMap("", "00");
	}

	/**
	 * 发答案 计算结果push出去
	 * 
	 * @return
	 */
	public String SendAnswer(QuestionParam param) {
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(param.getActivityId());
		AnswerData answerData = QuestionCache.getAnsweData(param.getActivityId(), questionStatus.getQuestionN());
		int subjectId = param.getSubjectId();
		Answer_subject question = StaticService.getSubject(subjectId);

		// 通过socketserver公布答案
		questionService.sendAnswer(answerData, question, questionStatus);
		return getClienSucMap("发送成功", "00");
	}
	
	
	/**
	 * 查询用户当前答题状况(答题正确否、能用复活卡否--->判定生死)； 1、多方条件判断 2、此处无需计算，直接获取之前的计算结果即可
	 * 
	 * @return
	 */
	public String queryAccountInfo(QuestionParam param) {
		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			logger.error("当前活动为空……，无法查询用户答题状况, accountId={}", param.getAccountId());
			return getClientErrMap("活动已经结束啦", Constants.STATUS_INTERNAL_ERROR);
		}
		int activityId = activity.getActivityId();

		String accountId = param.getAccountId();
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
		String UDTKey = QuestionCache.getAccountAnswerLogKey(activityId, accountId);
		int qN = questionStatus.getQuestionN();
		int qS = questionStatus.getQuestionS();
		if(qS < 1) {
			qN --; // 此时还未阅卷完毕，用户意外进场，应该去查询上一题结束后的状态
		}
		String userStatus = CacheUtil.getHashSetValue(UDTKey, String.valueOf(qN));
		AccountActivityStatus acS = new AccountActivityStatus();
		if (userStatus != null) {
			acS.setOrder(qN);
			if (userStatus.equals("1")) {
				acS.setLive(true);
				acS.setRAnswer(true);
			} else if (userStatus.equals("2")) {
				acS.setLive(true);
				acS.setRAnswer(false);
			} else if (userStatus.equals("12")) {
				acS.setLive(true);
				acS.setRAnswer(false);
			} else {
				acS.setLive(false);
				acS.setRAnswer(false);
			}
			// switch (userStatus) {
			// case "12":
			// }
		} else {
			if(qN >= 0) {
				acS.setLive(false);
				acS.setRAnswer(false);
//				acS.setAnswerOrder(-1);
			}
		}
		return getClienSucMap(acS, Constants.STATUS_REQUEST_SUCCESS);
	}

//	public String queryAccountInfo(QuestionParam param) {
//		Answer_activity activity = StaticService.getNowOrNextActivity();
//		if (activity == null) {
//			logger.error("当前活动为空……，无法查询用户答题状况, accountId={}", param.getAccountId());
//			return getClientErrMap("活动已经结束啦", Constants.STATUS_INTERNAL_ERROR);
//		}
//		int activityId = activity.getActivityId();
//
//		String accountId = param.getAccountId();
//		AccountActivityStatus accountStatus = QuestionCache.getAccountStatus(accountId, activityId);
//		logger.info("用户答题状态， activityId={}, accountStatus={}, accountId={}", activityId, accountStatus, param.getAccountId());
//		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
//		logger.info("活动状态, activityId={}, questionStatus={}, accountId={}", activityId, JSONObject.toJSONString(questionStatus),
//				param.getAccountId());
//
//		int questionN = questionStatus.getQuestionN();
//		if (questionStatus.getQuestionS() < 1) {
//			logger.info("********* 阅卷还未结束, 此时活动状态比用户状态要多1，这时来查询属于意外进场，应该将活动进度回退1题状态,, accountId={}");
//			questionN--;
//		}
//
//		if (accountStatus == null) {
//			logger.info("没有答题记录, activityId={}, accountId={}", activityId, accountId);
//			accountStatus = new AccountActivityStatus();
//			if (questionN > -1) {
//				logger.info("已经开题了，用户错过了第一题， 不能继续答题,activityId={},  accountId={}",activityId, param.getAccountId());
//				accountStatus.setLive(false);
//			}
//		} else {
//			// 用户漏答题目了
//			if (questionN > accountStatus.getOrder()) {
//				// 并且之前活着
//				if (accountStatus.isLive()) {
//					if (questionN - accountStatus.getOrder() > 1) {
//						logger.info("漏答超过两题题目，直接判负, activityId={}, accountId={}", activityId, param.getAccountId());
//						accountStatus.setAnswerOrder(-1);
//						accountStatus.setLive(false);
//						accountStatus.setRAnswer(false);
//					} else if (questionN - accountStatus.getOrder() == 1) {
//						logger.info("漏答了一题, activityId={}, accountId={}", activityId, param.getAccountId());
//						accountStatus.setAnswerOrder(-1);
//						accountStatus.setRAnswer(false);
//						accountStatus.setLive(QuestionCache.useCard(accountId, accountStatus.isCanUsedCard(), true));
//						accountStatus.setCanUsedCard(false);
//						// 更上答题序号
//						// TODO ，此处如果使用复活卡，那么需要更新到数据中 ┭┮﹏┭┮
//						accountStatus.setOrder(questionN);
//						QuestionCache.updateAccountStatus(accountId, accountStatus, activityId);
//
//						int usedCard = 0;
//						if (accountStatus.isLive() && !accountStatus.isRAnswer())
//							usedCard = 1;
//						int out = 0;
//						if (accountStatus.isLive()) {
//							// 漏题、活着。需要更新一些数据
//							String livePeopleKey = QuestionCache.getLivePeoPleKey(activityId, questionN);
//							CacheUtil.setHashSetValue(livePeopleKey, accountId, "1");
//							AnswerData nextAnswerData = new AnswerData();
//							AnswerData answerData = StaticService.getAnswerData(activityId, questionN);
//							nextAnswerData.copyFromLastData(answerData);
//							StaticService.setAnswerData(activityId, questionN + 1, nextAnswerData);
//						} else {
//							out = 1;
//						}
//						StaticService.updateAnswerData(activityId, questionN, 0, 0, 0, 1, usedCard, out);
//					}
//				} else {
//					// 之前挂了
//					accountStatus.setAnswerOrder(-1);
//				}
//			}
//		}
//
//		logger.info("用户活动状态：activityId={},  accountId={}, accountStatus={}", activityId, accountId, JSONObject.toJSONString(accountStatus));
//		return getClienSucMap(accountStatus, "00");
//
//	}

	/**
	 * 查询直播间信息； 1、获取在线人数，进行计算，更新当前 QustionData 2、如果目前
	 * ‘活动状态’还未开始答题，那么‘可答题总人数’=‘在线总人数’
	 * 
	 * @return
	 */
	public String queryRoomInfo(QuestionParam param) {
		// 根据时间，获取当前进行的活动
		Answer_activity activity = StaticService.getNowOrNextActivity();
		if (activity == null) {
			return getClientErrMap("当前活动未开启~", "05");
		}

		int totalLive = 0;
		try {
			// 在线人数，彻底修改来源，不再从 YJ 中获取了。单独的 key 
//			int questionN = QuestionCache.getQuestionStatus(activity.getActivityId()).getQuestionN();
//			String YJTotalKey = QuestionCache.getYJTotalKey(activity.getActivityId(), questionN);
//			totalLive = getInt(CacheUtil.getHashSetValue(YJTotalKey, "7"));
			String zxKey = QuestionCache.getZXKey();
			totalLive = getInt((String) CacheUtil.getFromRedis(zxKey));
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return getClienSucMap(activityService.getRoomInfo(param, activity, totalLive), "00");
	}


	/**
	 * 查询活动信息； 1、查数据库，返回活动信息 2、判断当前是否已经开始了答题了-->开放直播间入口|开放观战入口
	 * 
	 * @return
	 */
	public String queryHomeInfo(QuestionParam param) {

		ActivityInfo info = activityService.getActivitInfoFromHome(param);
		if (info != null) {
			return getClienSucMap(info, "00");
		} else {
			return getClienSucMap(info, "05");
			// return getClientErrMap("当前没有活动哟~", "05");
		}
	}

	/**
	 * 填写邀请码；
	 * 
	 * @param param
	 * @return
	 */
	public String fillInCode(QuestionParam param) {
		String code = param.getInviteCode();
		code = code.toUpperCase(); // all UP
		String codeKey = QuestionCache.getCodeCacheKey(code);
		String accountId = (String) CacheUtil.getFromRedis(codeKey);
		if (accountId == null) {
			logger.info("无效的邀请码, accountId={}, code={}, codeKey={}", param.getAccountId(), code, codeKey);
			return getClienSucMap("无效的邀请码", Constants.STATUS_PARAM_ERROR);
		}

		AccountInfo authorAccount = QuestionCache.getAccountInfo(param.getAccountId(), true);
		if (authorAccount.getCanFillCode() == 0) {
			logger.info("不再允许填写邀请码了, accountId={}", param.getAccountId());
			return getClienSucMap("不再允许填写邀请码了", Constants.STATUS_PARAM_ERROR);
		}
		AccountInfo incrAccount = QuestionCache.getAccountInfo(accountId, true);

		if (accountId.equals(param.getAccountId())) {
			return getClienSucMap("不能填写自己的邀请码", Constants.STATUS_PARAM_ERROR);
		}

		logger.info("有效的邀请码, 各自赏一枚复活卡, 主动填写的人authorAccount={}, 被填的人accountId={}", param.getAccountId(), accountId);
		authorAccount.setCardNum(authorAccount.getCardNum() + 1);
		authorAccount.setCanFillCode(0);
		incrAccount.setCardNum(incrAccount.getCardNum() + 1);

		QuestionCache.updateAccountInfo(param.getAccountId(), authorAccount);
		QuestionCache.updateAccountInfo(accountId, incrAccount);
		return getClienSucMap("fill code success", Constants.STATUS_REQUEST_SUCCESS);
	}
	
	/*
	 * 获取用户私有信息 
	 */
	public String getAccountInfo(QuestionParam param) {
		JSONObject json = new JSONObject();
		if (StringUtils.isNoneBlank(param.getAccountId())) {
			AccountInfo accInfo = QuestionCache.getAccountInfo(param.getAccountId(), true);
			json.put("relive", accInfo.getCardNum()); // 用户复活卡数量
			json.put("inviteCode", accInfo.getInviteCode());
			json.put("canFillCode", accInfo.getCanFillCode());
		}
		return getClienSucMap(json, Constants.STATUS_REQUEST_SUCCESS);
	}
	
	/*
	 * 获取用户私有信息 
	 */
	public String getQAccountInfo(QuestionParam param) {
		JSONObject json = new JSONObject();
		if (StringUtils.isNoneBlank(param.getAccountId())) {
			AccountInfo accInfo = QuestionCache.getQAccountInfo(param.getAccountId());
			if(accInfo == null) {
				return getClienSucMap(json, Constants.STATUS_REQUEST_SUCCESS);
			}
			json.put("relive", accInfo.getCardNum()); // 用户复活卡数量
			json.put("inviteCode", accInfo.getInviteCode());
			json.put("canFillCode", accInfo.getCanFillCode());
		}
		return getClienSucMap(json, Constants.STATUS_REQUEST_SUCCESS);
	}
	
	private int getInt(String hashSetValue) {
		if (hashSetValue == null)
			return 0;
		try {
			return Integer.parseInt(hashSetValue);
		} catch (Exception e) {
			logger.error("从redis获取整型数出错， value={}", hashSetValue, e);
			return 0;
		}
	}

	public static void main(String args[]) {
		String code = "12312a";
		code = code.toUpperCase();
		System.out.println(code);
		
		switch (code) {
		case "1":
			System.out.println("1");
			break;
		case "12312a":
			System.out.println("123");
			break;
		default:
			System.out.println("都不是");
			break;
		}
	}

	/*
	 * 修改第一题的总可答题人数(机器人数)
	 */
	public String changeFirstAnswerNum(int totalAnswerNum) {
		Answer_activity activity = StaticService.getNowOrNextActivity();
		String YJRobotKey = QuestionCache.getYJRobotKey(activity.getActivityId(), 0);
		CacheUtil.setHashSetValue(YJRobotKey, "6", totalAnswerNum + "");
		
		String YJRobotKey1 = QuestionCache.getYJRobotKey(activity.getActivityId(), -1);
		CacheUtil.setHashSetValue(YJRobotKey1, "6", totalAnswerNum + "");
		
		return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
	}

	/*
	 * 修改在线人数
	 */
	public String changeOnlineNum(int totalAnswerNum) {
//		Answer_activity activity = StaticService.getNowOrNextActivity();
//		if (activity != null) {
			String zxKey = QuestionCache.getZXKey();
			CacheUtil.setToRedis(zxKey, -1, totalAnswerNum + "");
//		} else {
//			return getClientErrMap("当前没有活动在进行", Constants.STATUS_REQUEST_SUCCESS);
//		}
		return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
	}

	/*
	 * 查询真实的在线人数
	 */
	public String getOnlineNum() {
		Object realiveO = CacheUtil.getPubClientNumber();
		int realLive = 0;
		if (realiveO != null) {
			realLive = Integer.parseInt((String) CacheUtil.getPubClientNumber());
		}
		JSONObject j = new JSONObject();
		j.put("onlineNum", realLive);
		return getClienSucMap(j, Constants.STATUS_REQUEST_SUCCESS);
	}
}
