package com.bus.chelaile.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
	public static final Map<Integer, Integer> ORDER_SUBJECT = New.hashMap(); // 题目真正的序号，与，id，对应关系

	private static final List<Answer_activity> NOW_ACTIVITY = New.arrayList();
	
	// TODO
	// 测试通过后，改为 redis 存储。 需要同时取活动和题目作为key, activityId#questionN
//	private static final Map<String, AnswerData> ANSWER_DATAS = New.hashMap(); // 答题数据
																				// key=题目序号

	public static void clearCache() {
		ALL_ACTIVITIES.clear();
		ALL_QUESTIONS.clear();
		ORDER_SUBJECT.clear();
		NOW_ACTIVITY.clear();
	}
	
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

	/**
	 * 获取所有有效活动（正在直播和当前时间之后）
	 * 
	 * @return
	 */
	public static Map<Integer, Answer_activity> getAllActivity() {
		if (ALL_ACTIVITIES == null || ALL_ACTIVITIES.size() == 0) {
			logger.error("活动库中没有找到活动");
			return null;
		}
		return ALL_ACTIVITIES;
	}

	public static String addActivity(Answer_activity activity) {
		ALL_ACTIVITIES.put(activity.getActivityId(), activity);
		return activity.getActivityId() + "";
	}

	public static String addSubject(Answer_subject subject) {
		ALL_QUESTIONS.put(subject.getId(), subject);
		return subject.getId() + "";
	}

	public static AnswerData getAnswerData(int activityId, int questionStatusN) {
//		String key = activityId + "#" + questionStatusN;
//		AnswerData answerData;
//		if (ANSWER_DATAS.containsKey(key)) {
//			answerData = ANSWER_DATAS.get(key);
//		} else {
//			answerData = new AnswerData();
//		}
//		return answerData;
		
		return QuestionCache.getAnsweData(activityId, questionStatusN);
	}

	public static void setAnswerData(int activityId, int questionStatusN, AnswerData answerData) {
//		String key = activityId + "#" + questionStatusN;
//		ANSWER_DATAS.put(key, answerData);
		
		QuestionCache.setAnsweData(activityId, questionStatusN, answerData);
	}

	/**
	 * 设置题目的生效时间、realOrder，发题的时候产生
	 * 
	 * @param subjectId
	 */
	public static boolean setEffective(int subjectId, ActivityStatus questionStatus) {

		Answer_subject question = getSubject(subjectId);
		if (null != question) {
			question.setEffectStartTime(System.currentTimeMillis());
			question.setEffectEndTime(System.currentTimeMillis() + EFFECTIVE_TIME);
			question.setRealOrder(questionStatus.getQuestionN());
			ORDER_SUBJECT.put(questionStatus.getQuestionN(), question.getId());
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
	public static void updateAnswerData(int activityId, int questionStatusN, int pAnswer, boolean hasUsedCard, boolean isLive) {
//		String key = activityId + "#" + questionStatusN;
		AnswerData answerData = getAnswerData(activityId, questionStatusN);
		answerData.addOneRecord(questionStatusN, pAnswer, hasUsedCard, isLive);

//		ANSWER_DATAS.put(key, answerData);
		QuestionCache.setAnsweData(activityId, questionStatusN, answerData);
	}


	/*
	 * 根据长连接，更新在线人数
	 */
	public static int updateAnswerDataFromLivePeople(int realLive, int activityId, boolean isSendFirstSubject) {
		// 查询当前 答题活动状态
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
		Answer_activity activity = ALL_ACTIVITIES.get(activityId);
		AnswerData answerData = getAnswerData(activityId, questionStatus.getQuestionN());
//		logger.info("更新前的答题数据为：{}", JSONObject.toJSON(answerData));

//		logger.info("根据长连接数，更新当前在线人数，isSendFirstSubject={}, questionStatus={}", isSendFirstSubject,
//				JSONObject.toJSON(questionStatus));
		answerData.updateWatchLiving(realLive, activity.getRobotMultiple());
		if (isSendFirstSubject || questionStatus.gameNotBegin()) { // 还未开始答题，可答题人数=在线人数
			logger.info("发送第一道题的时候，或者，还未开始答题，设置可答题人数=在线人数");
			answerData.copyAnswerFromWatch();
		}

//		logger.info("更新后的答题数据为：{}", JSONObject.toJSON(answerData));
//		String key = activityId + "#" + questionStatus.getQuestionN();
//		ANSWER_DATAS.put(key, answerData);
		QuestionCache.setAnsweData(activityId, questionStatus.getQuestionN(), answerData);

		return answerData.getWatchLiving().getTotal().get();
	}

	/*
	 * 获取当前进行的活动
	 */
	public static Answer_activity getNowOrNextActivity() {
		// 如果有正在进行的直播，直接返回即可
		if (NOW_ACTIVITY.size() > 1 && NOW_ACTIVITY.get(0).getIsonLive() == 1) {
			return NOW_ACTIVITY.get(0);
		}

		Map<Integer, Answer_activity> map = getAllActivity();
		if(map == null) {
			return null;
		}
		Answer_activity activity = null;

		// 获取即将或者正在进行的活动
		List<Answer_activity> coming_activities = New.arrayList();
		int isOnlive = 0;

		for (Entry<Integer, Answer_activity> entry : map.entrySet()) {
			Answer_activity value = entry.getValue();
			if (value.getStatus() == 1) { // 正在进行
				activity = value;
				isOnlive = 1;
				break;
			}
			if (value.getStatus() == 0) {
				coming_activities.add(value);
			}
		}

		// 当前没有正在进行的直播
		if (activity == null) {
			if (coming_activities.size() > 0) {
				Collections.sort(coming_activities, ACTIVITY_COMPARATOR);
				activity = coming_activities.get(0);
				// 开场前5分钟改为 ‘可以进入直播间’
				if (DateUtil.isbefore5min(activity.getStartTime())) {
					isOnlive = 1;
				}
			} else {
				logger.info("没有正在进行的，或者即将进行的活动");
				return null;
			}
		}

		activity.setIsonLive(isOnlive);
		NOW_ACTIVITY.add(activity);
		logger.info("当前活动id:{}", activity.getActivityId());
		return activity;
	}

	
	/**
	 * 应该按照优先级倒叙排序
	 */
	private static final Comparator<Answer_activity> ACTIVITY_COMPARATOR = new Comparator<Answer_activity>() {
		@Override
		public int compare(Answer_activity o1, Answer_activity o2) {
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			return o1.getStartTime().compareTo(o2.getStartTime());
		}
	};


	/*
	 * 获取最近一次的活动
	 */
	// public static Answer_activity getNextActivity() {
	// int id;
	// for(Answer_activity activity :ALL_ACTIVITIES.values()) {
	//
	//
	// }
	// }
}
