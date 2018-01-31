package com.bus.chelaile.service;

import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

public class ServiceManager {
	@Autowired
	private PublishDataService questionService;
	@Autowired
	private PublishDataService publishDataService;
	@Autowired
	private ActivityService activityService;

	private static final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(4);

	protected static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

	static HashMap<Integer, String> rooms = new HashMap<Integer, String>();
	private static final String QUESTION_CHANNEL = "ws";

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
		// Answer_activity activity =
		// StaticService.getActivity(param.getActivityId());
		if (question == null) {
			return getClientErrMap("发题失败，题目为空 , id=" + subjectId, Constants.STATUS_PARAM_ERROR);
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

		Map<String, PubQuestion> sendQustion = New.hashMap();
		sendQustion.put("q", pubQuestion);
		Long pubResult = CacheUtil.publish(QUESTION_CHANNEL, JSONObject.toJSONString(sendQustion));
		if (pubResult != 0) {
			logger.info("发题成功, id={}", subjectId);
		} else {
			getClientErrMap("发题失败 , 题目是：" + question.getSubject(), Constants.STATUS_INTERNAL_ERROR);
		}

		QuestionCache.addPubQuestion(questionStatus, question.getActivityId());
		StaticService.setEffective(subjectId, questionStatus); // 设置题目生效时间和realOrder

		// 根据生成的题目有效截止时间，来启动执行‘消费答卷’的任务
		exec.schedule(new CalculateAnswerThread(subjectId, questionStatus, question.getActivityId()), StaticService.EFFECTIVE_TIME,
				TimeUnit.MILLISECONDS);
		return getClienSucMap(new JSONObject(), Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 答题 1、题目塞进队列 2、[10s]后 开始计算 3、保存计算结果到redis，供运营后台查询，并可修改
	 * 
	 * @return
	 */
	public String answerQuestion(QuestionParam param) {

		int questionId = param.getSubjectId();
		Answer_subject subject = StaticService.getSubject(questionId);
		long startTime = subject.getEffectStartTime();
		long endTime = subject.getEffectEndTime();

		if (System.currentTimeMillis() > endTime || System.currentTimeMillis() < startTime) {
			logger.info("");
			System.out.println("超时啦, subjectId=" + param.getSubjectId());
			return getClientErrMap("已经超时啦", Constants.STATUS_FUNCTION_NOT_ENABLED);
		}
		exec.execute(new PushAnswerLogThread(param));

		return getClienSucMap("", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 运营后台，查询计算结果
	 * 
	 * @return
	 */
	public String queryQuestionData(QuestionParam param) {

		// 查询当前 答题活动状态
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(param.getActivityId());
		Answer_activity activity = StaticService.getActivity(param.getActivityId());
		logger.info("活动不存在, activityId={}", param.getActivityId());
		
		AnswerData answerData = StaticService.getAnswerData(activity.getActivityId(), questionStatus.getQuestionN());
		JSONObject responseJ = new JSONObject();
		responseJ.put("canChanged", 1);
		if (questionStatus.hasToCalculated()) {
			System.out.println("开始计算 ， questionStatus=" + JSONObject.toJSONString(questionStatus));
			answerData.calcuRobots(questionStatus.getQuestionN(), activity.getRobotMultiple());

			// 更新当前态的数目, 供查询使用
			StaticService.setAnswerData(activity.getActivityId(), questionStatus.getQuestionN(), answerData);

			// 设置下一个题目的初始答题数据
			AnswerData nextAnswerData = new AnswerData();
			nextAnswerData.copyFromLastData(answerData);
			StaticService.setAnswerData(activity.getActivityId(), questionStatus.getQuestionN() + 1, nextAnswerData);

			logger.info("计算结束 ");
			questionStatus.setQuestionS(2);
			QuestionCache.updateQuestionStatus(param.getActivityId(), questionStatus);
		} else if (!questionStatus.hasCalculated()) { // !=2 的情况，都不可以修改
			responseJ.put("canChanged", 0);
			logger.info("当前状态不可修改数据：questionStatus={}", JSONObject.toJSONString(questionStatus));
		}

		responseJ.put("index", questionStatus.getQuestionN() + 1);
		responseJ.put("answerDate", answerData);
		return getClienSucMap(responseJ, "00");
	}

	
	 /**
	 * 运营后台，修改之前的计算结果
	 * @return
	 */
	 public String changeRobotNum(int index,int activityId,int watchingRobot,int option1Robot,int option2Robot,int option3Robot,int notAnsRobot,int reLivingRobot) {
		 
		 Answer_activity activity = StaticService.getNowOrNextActivity();
		 if(activity == null) {
			 logger.info("当前没有正在进行的活动");
			 return getClienSucMap("", "00");
		 }
		 ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());
		 AnswerData answerData = StaticService.getAnswerData(activity.getActivityId(), questionStatus.getQuestionN());
		 	
		 if(activityId != activity.getActivityId()) {
			 logger.info("######## 调整数据异常, 请求参数与缓存不一致");
			 logger.error("调整数据异常 ");
			 return getClienSucMap("", "00");
		 }
		 
		 answerData.changeData(watchingRobot,option1Robot,option2Robot,option3Robot,notAnsRobot,reLivingRobot);
		 StaticService.setAnswerData(activity.getActivityId(), questionStatus.getQuestionN(), answerData);
		 return getClienSucMap("", "00");
	 }
	
	/**
	 * 发答案 计算结果push出去
	 * 
	 * @return
	 */
	public String SendAnswer(QuestionParam param) {
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(param.getActivityId());
		AnswerData answerData = StaticService.getAnswerData(param.getActivityId(), questionStatus.getQuestionN());
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

		String accountId = param.getAccountId();
		AccountActivityStatus accountStatus = QuestionCache.getAccountStatus(accountId, activity.getActivityId());
		logger.info("用户答题状态， accountStatus={}", accountStatus);
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());
		logger.info("活动状态, questionStatus={}", JSONObject.toJSONString(questionStatus));
		if (accountStatus == null) {
			logger.info("没有答题记录, accountId={}", accountId);
			accountStatus = new AccountActivityStatus();
			if (questionStatus.getQuestionN() != -1) {
				logger.info("已经开题了，用户错过了第一题， 不能继续答题");
				accountStatus.setLive(false);
			}
		} else {
			// 用户漏答题目了，并且之前活着
			if ((questionStatus.getQuestionN() != accountStatus.getOrder()) && accountStatus.isLive()) {
				if (questionStatus.getQuestionN() - accountStatus.getOrder() > 1) {
					logger.info("漏答超过两题题目");
					accountStatus.setAnswerOrder(-1);
					// accountStatus.setRAnswer(false);
					accountStatus.setLive(false);
				} else if (questionStatus.getQuestionN() - accountStatus.getOrder() == 1) {
					logger.info("漏答了一题");
					accountStatus.setAnswerOrder(-1);
					accountStatus.setLive(QuestionCache.useCard(accountId, accountStatus.isCanUsedCard(), false));
				}
			}
		}

		System.out.println("用户活动状态： accountId=" + accountId + ", accountStatus="
				+ JSONObject.toJSONString(accountStatus));
		return getClienSucMap(accountStatus, "00");

	}

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
			logger.error("初始化房间的时候，未能找到活动信息");
			return getClientErrMap("当前活动未开启~", "00");
		}

		Object realiveO = CacheUtil.getPubClientNumber();
		int realLive = 0;
		if (realiveO != null) {
			realLive = Integer.parseInt((String) CacheUtil.getPubClientNumber());
		}

		logger.info("获取到的实际链接数是：realLive={}", realLive);
		int totaoLive = StaticService.updateAnswerDataFromLivePeople(realLive, activity.getActivityId(), false);

		return getClienSucMap(activityService.getRoomInfo(param, activity, totaoLive), "00");
	}

	/**
	 * 查询活动信息； 1、查数据库，返回活动信息 2、判断当前是否已经开始了答题了-->开放直播间入口|开放观战入口
	 * 
	 * @return
	 */
	public String queryHomeInfo(QuestionParam param) {

		ActivityInfo info = activityService.getActivitInfo(param);
		if (info != null) {
			return getClienSucMap(info, "00");
		} else {
			return getClientErrMap("当前没有活动哟~", "00");
		}
	}

	public static void main(String args[]) {

	}

	/**
	 * 填写邀请码；
	 * 
	 * @param param
	 * @return
	 */
	public String fillInCode(QuestionParam param) {
		String code = param.getInviteCode();

		// TODO 
		// 加上校验，不是所有人都有资格填写邀请码的 ！！！
		
		String codeKey = QuestionCache.getCodeCacheKey(code);
		String accountId = (String) CacheUtil.getFromRedis(codeKey);
		if (accountId == null) {
			logger.info("无效的邀请码, accountId={}, code={}, codeKey={}", param.getAccountId(), code, codeKey);
			return getClienSucMap("无效的邀请码", Constants.STATUS_PARAM_ERROR);
		}

		AccountInfo authorAccount = QuestionCache.getAccountInfo(param.getAccountId());
		if(authorAccount.getCanFillCode() == 0) {
			logger.info("不再允许填写邀请码了, accountId={}", param.getAccountId());
			return getClienSucMap("不再允许填写邀请码了", Constants.STATUS_PARAM_ERROR);
		}
		AccountInfo incrAccount = QuestionCache.getAccountInfo(accountId);

		logger.info("有效的邀请码, 各自赏一枚复活卡, 主动填写的人authorAccount={}, 被填的人accountId={}", param.getAccountId(), accountId);
		authorAccount.setCardNum(authorAccount.getCardNum() + 1);
		authorAccount.setCanFillCode(0);
		incrAccount.setCardNum(incrAccount.getCardNum() + 1);

		QuestionCache.updateAccountInfo(param.getAccountId(), authorAccount);
		QuestionCache.updateAccountInfo(accountId, incrAccount);
		return getClienSucMap("fill code success", Constants.STATUS_REQUEST_SUCCESS);
		
		
	}
}
