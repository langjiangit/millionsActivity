package com.bus.chelaile.thread;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerLog;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.model.account.AccountActivityStatus;
import com.bus.chelaile.service.StaticService;
import com.bus.chelaile.util.DateUtil;

public class CalculateAnswerThread implements Runnable {

	private int subjectId;
	// private int robotMultiple;
	private ActivityStatus questionStatus;
	private int activityId;

	Logger logger = LoggerFactory.getLogger(CalculateAnswerThread.class);

	public CalculateAnswerThread(int subjectId, ActivityStatus questionStatus, int activityId) {
		this.subjectId = subjectId;
		this.questionStatus = questionStatus;
		this.activityId = activityId;
	}

	/**
	 * 处理提交了当前题目答案的用户
	 * 需要提前校验用户之前的答题状况
	 */
	@Override
	public void run() {
		logger.info("开始阅卷！ ");
		try {
			System.out.println("开始阅卷！ " + DateUtil.getFormatTime(new Date(), "yyyy-MM-dd HH:mm:ss") 
					+ ", questionStatus=" + JSONObject.toJSONString(questionStatus));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Answer_subject answerSubject = StaticService.getSubject(subjectId);
		int rightAnswer = answerSubject.getAnswer();

		String livePeopleKey = QuestionCache.getLivePeoPleKey(activityId, questionStatus.getQuestionN());
		
		String key = QuestionCache.getAnswerLogListKey(subjectId);
		Map<String, String> allAnswers = CacheUtil.getHsetAll(key);
		if(allAnswers != null) {
			for(Entry<String, String> entry : allAnswers.entrySet()) {
				String answerLogStr = entry.getValue();
				if (answerLogStr == null) {
					break;
				}
//				while (true) {
				// 取试卷
//				String answerLogStr = CacheUtil.lpop(key);
				System.out.println("取到试卷：answerLogStr=" + answerLogStr);
				logger.info("取到试卷： answerLogStr={}", answerLogStr);
				AnswerLog answer = JSONObject.parseObject(answerLogStr, AnswerLog.class);

				// 用户答题状态记录
				String accountId = answer.getAccountId();
				AccountActivityStatus accountStatus = QuestionCache.getAccountStatus(accountId, activityId);
				logger.info("用户答题状态: accountId={}, accountStatus={}", answer.getAccountId(), accountStatus);
				
				// 之前没有找到答题记录，那么必须从第一题开始，否则作废
				if (accountStatus == null) {
					if (questionStatus.getQuestionN() == 0) {
						accountStatus = new AccountActivityStatus();  // 初始化用户的答题状态
						accountStatus.setAnswerOrder(answer.getpAnswer());
						if (answer.getpAnswer() == rightAnswer) { // 答对
							logger.info("第一题，答对， accountId={}", answer.getAccountId());
							accountStatus.setRAnswer(true);
							accountStatus.setLive(true);
							accountStatus.setCanUsedCard(true);
							accountStatus.setOrder(0);
						} else { // 答错
							logger.info("第一题，答错， accountId={}", answer.getAccountId());
							accountStatus.setRAnswer(false);
							accountStatus.setLive(QuestionCache.useCard(accountId, false, true)); // 根据是否可以使用复活卡，决定生死
							accountStatus.setCanUsedCard(false); // 不再能够使用复活卡了
							accountStatus.setOrder(0);
						}
					} else {
						System.out.println("用户错过了第一题，accountId=" +accountId + ", 当前进行到题序：" + questionStatus.getQuestionN());
						logger.error("accountId={}，当前进行到第{}题，用户还没有过答题记录：用户错过了第一题！", accountId,
								questionStatus.getQuestionN());
						return;
					}
				}

				else if(! accountStatus.isLive()) {
					logger.error("出现已经挂掉的用户答题的情况： accountId={}, accountStatus={}", accountId, accountStatus);
					return;
				}
				
				/** 之前有答题记录 **/

				// 超过两题未答
				else if (answerSubject.getRealOrder() - accountStatus.getOrder() > 2) {
					// order 不做改动
					// TODO 
					accountStatus.setRAnswer(answer.getpAnswer() == rightAnswer);
					accountStatus.setRAnswer(false);
					accountStatus.setLive(false);
					accountStatus.setCanUsedCard(false);
					accountStatus.setAnswerOrder(-1);
					logger.info("(连续断网两题才会发生的情况, 如果连续不答题，不会走到这里[最新改动，连续两次未答题也不会走到这里，"
							+ "初始化房间的时候，会进行生死查询，用户已经不可能再提交答案了]) accountId={}，当前进行到第{}题，用户只答到第{}题，用户错过了两题以上！", accountId,
							questionStatus.getQuestionN(), accountStatus.getOrder());
				}

				// 有一题未答
				else if (answerSubject.getRealOrder() - accountStatus.getOrder() == 2) {
					accountStatus.setLive(QuestionCache.useCard(accountId, accountStatus.isCanUsedCard(), true)); // 使用复活卡
					accountStatus.setCanUsedCard(false);
					if(accountStatus.isLive()) { // 漏题结账后，还活着，看当前这一题答对否
						accountStatus.setRAnswer(answer.getpAnswer() == rightAnswer);
						accountStatus.setLive(accountStatus.isRAnswer());
						accountStatus.setOrder(answerSubject.getRealOrder());
						accountStatus.setAnswerOrder(answer.getpAnswer());
					} else {
						// 用户挂掉了
						logger.info("如果初始化查询生死正确，用户也不会走到这一步！ accountId={}, questionStatus={}", 
								accountId, JSONObject.toJSONString(questionStatus));
						accountStatus.setRAnswer(false);
						accountStatus.setAnswerOrder(-1);
					}
					logger.info("accountId={}，当前进行到第{}题，用户只答到第{}题，用户错过了某一题！", accountId, questionStatus.getQuestionN(),
							accountStatus.getOrder());
				}

				// 正常情况挨个答题
				else if (answer.getpAnswer() == rightAnswer) { // 答对
					accountStatus.setRAnswer(true);
					accountStatus.setLive(true);
					accountStatus.setOrder(answerSubject.getRealOrder());
					accountStatus.setAnswerOrder(answer.getpAnswer());
				} else if (answer.getpAnswer() != rightAnswer) { // 答错
					accountStatus.setRAnswer(false);
					accountStatus.setLive(QuestionCache.useCard(accountId, accountStatus.isCanUsedCard(), true));
					accountStatus.setCanUsedCard(false);
					accountStatus.setOrder(answerSubject.getRealOrder());
					accountStatus.setAnswerOrder(answer.getpAnswer());
				} else {
					logger.error("遇到未考虑的情况， accountId={}", accountId);
				}

				// 更新用户的答题状态
				QuestionCache.updateAccountStatus(accountId, accountStatus, activityId);
				
				// 更新答题总数据
				// TODO 
				// 改成： 循环阅卷完毕后，统一更新。因为涉及到多台服务器之间的冲突问题
				StaticService.updateAnswerData(activityId, questionStatus.getQuestionN(), answer.getpAnswer(), accountStatus.isLive()
						&& !accountStatus.isRAnswer(), accountStatus.isLive());
				
				// 记录答对题目的人
				if(accountStatus.isLive()) {
					CacheUtil.setHashSetValue(livePeopleKey, accountId, "1");
				}
				
			}
		}
		
		

		// 阅卷结束，修改状态
//		try {
//			Thread.sleep();
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		logger.info("阅卷结束！ ");
		System.out.println("阅卷结束！ " + ", questionStatus=" + JSONObject.toJSONString(questionStatus));
		questionStatus.setQuestionS(1);

		QuestionCache.updateQuestionStatus(answerSubject.getActivityId(), questionStatus);
	}


	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public ActivityStatus getQuestionStatus() {
		return questionStatus;
	}

	public void setQuestionStatus(ActivityStatus questionStatus) {
		this.questionStatus = questionStatus;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
}
