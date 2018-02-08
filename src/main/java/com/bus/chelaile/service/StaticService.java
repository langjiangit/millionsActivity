package com.bus.chelaile.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.util.DateUtil;
import com.bus.chelaile.util.New;

public class StaticService {

	private static final Logger logger = LoggerFactory.getLogger(StaticService.class);

	private static final Map<Integer, Answer_activity> ALL_ACTIVITIES = New.hashMap(); // 所有活动
	private static final Map<Integer, Answer_subject> ALL_QUESTIONS = New.hashMap(); // 题库
	public static final long EFFECTIVE_TIME = 20 * 1000; // 答题时间，12s
	public static final Map<Integer, Integer> ORDER_SUBJECT = New.hashMap(); // 题目真正的序号，与，id，对应关系
	public static final Set<Integer> SENDED_SUBJECT = New.hashSet();
	
	private static final List<Answer_activity> NOW_ACTIVITY = New.arrayList();
	
	// 测试通过后，改为 redis 存储。 需要同时取活动和题目作为key, activityId#questionN
//	private static final Map<String, AnswerData> ANSWER_DATAS = New.hashMap(); // 答题数据
																				// key=题目序号

	public static void clearCache() {
		ALL_ACTIVITIES.clear();
		ALL_QUESTIONS.clear();
		ORDER_SUBJECT.clear();
		NOW_ACTIVITY.clear();
		SENDED_SUBJECT.clear();
	}
	
	public static Answer_subject getSubject(int subjectId) {
		if (subjectId == -1 || ALL_QUESTIONS == null || ALL_QUESTIONS.size() == 0
				|| !ALL_QUESTIONS.containsKey(subjectId)) {
			logger.error("题库中没有找到题目, subjectId={}", subjectId);
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



//	public static void setAnswerData(int activityId, int questionStatusN, AnswerData answerData) {
////		String key = activityId + "#" + questionStatusN;
////		ANSWER_DATAS.put(key, answerData);
		
//		QuestionCache.setAnsweData(activityId, questionStatusN, answerData);
//	}

	/**
	 * 设置题目的生效时间、realOrder，发题的时候产生
	 * 
	 * @param subjectId
	 */
	public static boolean setEffective(int subjectId, ActivityStatus questionStatus) {

		Answer_subject question = getSubject(subjectId);
		SENDED_SUBJECT.add(subjectId);
		if (null != question) {
			question.setEffectStartTime(System.currentTimeMillis());
			question.setEffectEndTime(System.currentTimeMillis() + EFFECTIVE_TIME);
			question.setRealOrder(questionStatus.getQuestionN());
			setSubjectId(questionStatus.getQuestionN(), question.getId());
		} else {
			return false;
		}

		logger.info("设置题目生效结束, question={}", JSONObject.toJSONString(question));
		return true;
	}
	
	public static int getSubjectId(int realOrder) {
		if(ORDER_SUBJECT.containsKey(realOrder)) {
			return ORDER_SUBJECT.get(realOrder);
		} else {
			return -1;
		}
	}
	
	public static void setSubjectId(int realOrder, int subject) {
		ORDER_SUBJECT.put(realOrder, subject);
	}


//	/**
//	 * 
//	 * @param activityId
//	 * @param questionStatusN
//	 * @param option1RealNum
//	 * @param option2RealNum
//	 * @param option3RealNum
//	 * @param noAnswerRealNum
//	 * @param usedCardReadNum
//	 * @param outRealNum
//	 */
//	public static void updateAnswerData(int activityId, int questionStatusN, 
//			int option1RealNum, int option2RealNum, int option3RealNum, 
//			int noAnswerRealNum, int usedCardReadNum, int outRealNum) {
//		logger.info("计算完毕，更新数据前, questionStatusN={}, option1RealNum={}, option2RealNum={}, option3RealNum={},"
//				+ "noAnswerRealNum={}, usedCardReadNum={}, outRealNum={}", questionStatusN, option1RealNum,
//				option2RealNum, option3RealNum, noAnswerRealNum, usedCardReadNum, outRealNum);
//		
//		AnswerData answerData = getAnswerData(activityId, questionStatusN);
//		logger.info("更新答题数据前：{}", JSONObject.toJSONString(answerData));
//		answerData.addRealRecord(option1RealNum, option2RealNum, option3RealNum, noAnswerRealNum, usedCardReadNum, outRealNum);
//		logger.info("更新答题数据后：{}", JSONObject.toJSONString(answerData));
//		QuestionCache.setAnsweData(activityId, questionStatusN, answerData);
//		
//		
//		
////		String key = activityId + "#" + questionStatusN;
////		answerData.addOneRecord(questionStatusN, pAnswer, hasUsedCard, isLive);
////		ANSWER_DATAS.put(key, answerData);
//	}


	/*
	 * 根据长连接，更新在线人数
	 */
	
//	public static int updateAnswerDataFromLivePeople(int realLive, int activityId) {
//		// 查询当前 答题活动状态
//		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
//		Answer_activity activity = ALL_ACTIVITIES.get(activityId);
//		AnswerData answerData = QuestionCache.getAnsweData(activityId, questionStatus.getQuestionN());
////		logger.info("更新前的答题数据为：{}", JSONObject.toJSON(answerData));
//
//		answerData.updateWatchLiving(realLive, activity.getRobotMultiple());
//		if (questionStatus.getQuestionN() == 0) {
//			logger.info("第一题，设置可答题人数等于在线人数");
//			answerData.copyAnswerFromWatch();
//		}
//
////		logger.info("更新后的答题数据为：{}", JSONObject.toJSON(answerData));
////		String key = activityId + "#" + questionStatus.getQuestionN();
////		ANSWER_DATAS.put(key, answerData);
//		QuestionCache.setAnsweData(activityId, questionStatus.getQuestionN(), answerData);
//
//		return answerData.getWatchLiving().getTotal().get();
//	}

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
//		logger.info("当前活动id:{}", activity.getActivityId());
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
