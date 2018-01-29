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
import com.bus.chelaile.model.client.JsonStr;
import com.bus.chelaile.model.pubmsg.PubQuestion;
import com.bus.chelaile.mvc.QuestionParam;
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
//		Answer_activity activity = StaticService.getActivity(param.getActivityId());
		if (question == null) {
			return getClientErrMap("发题失败，题目为空 , id=" + subjectId, Constants.STATUS_PARAM_ERROR);
		}

		// 查询当前 答题活动状态
		ActivityStatus questionStatus = QuestionCache.getQuestionStatusKey(question.getActivityId());

		// pushlish 题目
		PubQuestion pubQuestion = new PubQuestion(questionStatus, question);
		try {
			System.out.println("发送的题目是" + JSONObject.toJSONString(pubQuestion) + ", 时间: " + DateUtil.getFormatTime(new Date(), "yyyy-MM-dd HH:mm:ss"));
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
		exec.schedule(new CalculateAnswerThread(subjectId, questionStatus), StaticService.EFFECTIVE_TIME,
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
		ActivityStatus questionStatus = QuestionCache.getQuestionStatusKey(param.getActivityId());
		AnswerData answerData = StaticService.getAnswerData(questionStatus.getQuestionN());
		Answer_activity activity = StaticService.getActivity(param.getActivityId());
		if (questionStatus.hasToCalculated()) {
			System.out.println("开始计算 ， questionStatus=" + JSONObject.toJSONString(questionStatus));
			answerData.calcuRobots(questionStatus.getQuestionN(), activity.getRobotMultiple());
			
			// 更新当前态的数目, 供查询使用
			StaticService.setAnswerData(questionStatus.getQuestionN(), answerData);
			
			AnswerData nextAnswerData = new AnswerData();
			nextAnswerData.copyFromLastData(answerData);
			StaticService.setAnswerData(questionStatus.getQuestionN() + 1, nextAnswerData);
			
			logger.info("计算结束 ");
			questionStatus.setQuestionS(2);
			QuestionCache.updateQuestionStatus(param.getActivityId(), questionStatus);
			
			return getClienSucMap(answerData, "00");
			
		} else if(questionStatus.hasCalculated()) {
			return getClienSucMap(answerData, "00");
		} else {
			logger.info("当前状态不可修改数据：questionStatus={}", JSONObject.toJSONString(questionStatus));
			return getClientErrMap("不可修改状态", "03");
		}
	}

	//
	// /**
	// * 运营后台，修改之前的计算结果
	// * @return
	// */
	// public String changeRobotNum() {
	//
	// }
	//
	/**
	 * 发答案 计算结果push出去
	 * 
	 * @return
	 */
	public String SendAnswer(QuestionParam param) {
		ActivityStatus questionStatus = QuestionCache.getQuestionStatusKey(param.getActivityId());
		AnswerData answerData = StaticService.getAnswerData(questionStatus.getQuestionN());
		int subjectId = param.getSubjectId();
		Answer_subject question = StaticService.getSubject(subjectId);
		
		// 通过socketserver公布答案
		questionService.sendAnswer(answerData, question, questionStatus);
		return getClienSucMap("发送成功", "00");
	}

	 /**
	 * 查询用户当前答题状况(答题正确否、能用复活卡否--->判定生死)；
	 * 1、多方条件判断
	 * 2、此处无需计算，直接获取之前的计算结果即可
	 * @return
	 */
	 public String queryAccountInfo(QuestionParam param) {
	
		 String accountId = param.getAccountId();
		 AccountActivityStatus accountStatus = QuestionCache.getAccountStatus(accountId);
		 if(accountStatus == null) {
			 logger.info("没有答题记录, accountId={}", accountId);
			 return getClienSucMap(new JSONObject(), Constants.STATUS_PARAM_ERROR);
		 }
		 System.out.println("用户活动状态： accountId=" + accountId + ", accountStatus=" + JSONObject.toJSONString(accountStatus));
		 return getClienSucMap(accountStatus, "00");
		 
	 }

	/**
	 * 查询活动信息；1、查数据库，返回活动信息 2、判断当前是否已经开始了答题了-->开放直播间入口|开放观战入口
	 * 
	 * @return
	 */
//	public String queryActivityInfo() {
//		
//		List<Answer_activity> activities = 
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
		Answer_activity activity = StaticService.getActivity(new Date());
		
		// TODO 从长链接服务获取该人数
		int realLive = 23121; 
		
		StaticService.updateAnswerDataFromLivePeople(realLive, param.getActivityId());

		return getClienSucMap(activityService.getRoomInfo(param), "00");
	}

	
	public static void main(String args[]) {

	}

	
	/**
	 * 填写邀请码；
	 * @param param
	 * @return
	 */
	public String fillInCode(QuestionParam param) {
		
		
		
		
		return null;
	}
}
