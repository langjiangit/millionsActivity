/**
 * @author quekunkun
 *
 */
package com.bus.chelaile.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.AnswerLog;
import com.bus.chelaile.mvc.QuestionParam;

public class PushAnswerLogThread implements Runnable {

	private QuestionParam param;
	private int questionStatusN;
	private int activityId;

	public PushAnswerLogThread(QuestionParam param, int questionStatusN, int activityId) {
		this.param = param;
		this.questionStatusN = questionStatusN;
		this.activityId = activityId;
	}

	private static final Logger logger = LoggerFactory.getLogger(PushAnswerLogThread.class);

	@Override
	public void run() {
		AnswerLog answerLog = new AnswerLog(param);
		String DTkey = QuestionCache.getAnswerLogListKey(questionStatusN, activityId);
		String field = param.getAccountId();;
		
		logger.info("单独收卷！key={},field={},answerLog={}", DTkey, field, JSONObject.toJSONString(answerLog));	
		CacheUtil.setHashSetValue(DTkey, field, JSONObject.toJSONString(answerLog));
		
//		String answerK = QuestionCache.getAnswerKey(param.getAccountId(), answerLog.getQuestionId());
//		if (CacheUtil.incrToCache(answerK, -1) == 1) {
			String dtPersonK = QuestionCache.getPeopleJJKey(activityId, questionStatusN);
			CacheUtil.lPush(dtPersonK, param.getAccountId());
//		} else {
//			logger.info("重复提交答案，拒绝入队列, accountId={}, subjectId={}", param.getAccountId(), answerLog.getQuestionId());
//		}
	}

	public int getQuestionStatusN() {
		return questionStatusN;
	}

	public void setQuestionStatusN(int questionStatusN) {
		this.questionStatusN = questionStatusN;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

}