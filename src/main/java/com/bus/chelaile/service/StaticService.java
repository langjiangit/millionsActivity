package com.bus.chelaile.service;

import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerData;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.New;

public class StaticService {

	private static final Logger logger = LoggerFactory.getLogger(StaticService.class);

	private static final Map<Integer, Answer_activity> ALL_ACTIVITIES = New.hashMap(); // 所有活动
	private static final Map<Integer, Answer_subject> ALL_QUESTIONS = New.hashMap(); // 题库
	public static final long EFFECTIVE_TIME = 12 * 1000; // 答题时间，12s

	private static final Map<Integer, AnswerData> ANSWER_DATAS = New.hashMap(); // 答题数据  key=题目序号

	public static Answer_subject getSubject(int subjectId) {
		if (subjectId == -1 || ALL_QUESTIONS == null || ALL_QUESTIONS.size() == 0
				|| !ALL_QUESTIONS.containsKey(subjectId)) {
			logger.error("题库中没有找到题目, subjectId={}, 题库={}", subjectId, ALL_QUESTIONS);
			return null;
		}

		return ALL_QUESTIONS.get(subjectId);
	}

	public static Answer_activity getActivity(int activityId) {
		if (activityId == -1 || ALL_ACTIVITIES == null || ALL_ACTIVITIES.size() == 0
				|| !ALL_ACTIVITIES.containsKey(activityId)) {
			logger.error("活动库中没有找到活动, activityId={}, 题库={}", activityId, ALL_ACTIVITIES);
			return null;
		}

		return ALL_ACTIVITIES.get(activityId);
	}
	
	public static String addActivity(Answer_activity activity) {
		ALL_ACTIVITIES.put(activity.getActivityId(), activity);
		return activity.getActivityId() + "";
	}
	
	public static String addSubject(Answer_subject subject) {
		ALL_QUESTIONS.put(subject.getId(), subject);
		return subject.getId() + "";
	}

	
	public static AnswerData getAnswerData(int questionStatusN) {
		AnswerData answerData;
		if (ANSWER_DATAS.containsKey(questionStatusN)) {
			answerData = ANSWER_DATAS.get(questionStatusN);
		} else {
			answerData = new AnswerData();
		}
		return answerData;
	}
	
	public static void setAnswerData(int questionStatusN, AnswerData answerData) {
		ANSWER_DATAS.put(questionStatusN, answerData);
	}
	
	
	/**
	 * 设置题目的生效时间、realOrder，发题的时候产生
	 * @param subjectId
	 */
	public static boolean setEffective(int subjectId, ActivityStatus questionStatus) {

		Answer_subject question = getSubject(subjectId);
		if (null != question) {
			question.setEffectStartTime(System.currentTimeMillis());
			question.setEffectEndTime(System.currentTimeMillis() + EFFECTIVE_TIME);
			question.setRealOrder(questionStatus.getQuestionN());
		} else {
			return false;
		}

		System.out.println("设置题目生效结束, question=" + JSONObject.toJSONString(question));
		logger.info("设置题目生效结束, question={}", JSONObject.toJSONString(question));
		return true;
	}

	/**
	 * 
	 * @param questionStatusN
	 *            当前活动的答题进程
	 * @param pAnswer
	 *            用户选的第几题
	 * @param hasUsedCard
	 *            用户是否使用了复活卡
	 */
	public static void updateAnswerData(int questionStatusN, int pAnswer, boolean hasUsedCard, boolean isLive) {

		AnswerData answerData = getAnswerData(questionStatusN);
		answerData.addOneRecord(questionStatusN, pAnswer, hasUsedCard, isLive);
		
		ANSWER_DATAS.put(questionStatusN, answerData);
	}

	/**
	 * 运营后台修改 答题数据
	 */
	public static void updateAnswerDataFromRobot() {

	}

	/*
	 * 根据长连接，更新在线人数
	 */
	public static void updateAnswerDataFromLivePeople(int realLive, int activityId) {
		int realAnswerNum = 0;
		// 查询当前 答题活动状态
		ActivityStatus questionStatus = QuestionCache.getQuestionStatusKey(activityId);
		if(questionStatus.isNotBegin()) { // 还未开始答题， 可答题人数=在线人数
			realAnswerNum = realLive;
		}
		
		AnswerData answerData = getAnswerData(questionStatus.getQuestionN());
		answerData.updateWatchLiving(realLive);
		
		
		if(realAnswerNum != 0) {
			answerData.updateAnswerNum(realAnswerNum);
		}
		
		ANSWER_DATAS.put(questionStatus.getQuestionN(), answerData);
	}

	/*
	 * 获取当前时间段内的活动
	 */
	public static Answer_activity getActivity(Date date) {
		for(Answer_activity an : ALL_ACTIVITIES.values()) {
			Date anDate = DateUtil.getDate(an.getStartTime(), "yyyy-MM-dd HH:mm:ss");
			if(anDate.before(new Date())) {	 // 活动已经开始
				return an;
			}
		}
		return null;
	}
	
	/*
	 * 获取最近一次的活动
	 */
//	public static Answer_activity getNextActivity() {
//		int id;
//		for(Answer_activity activity :ALL_ACTIVITIES.values()) {
//			
//			
//		}
//	}
}
