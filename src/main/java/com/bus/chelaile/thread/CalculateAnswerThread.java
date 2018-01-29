package com.bus.chelaile.thread;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerLog;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.model.account.AccountActivityStatus;
import com.bus.chelaile.model.account.AccountInfo;
import com.bus.chelaile.service.StaticService;
import com.bus.chelaile.util.DateUtil;

public class CalculateAnswerThread implements Runnable {

	private int subjectId;
	// private int robotMultiple;
	private ActivityStatus questionStatus;

	Logger logger = LoggerFactory.getLogger(CalculateAnswerThread.class);

	public CalculateAnswerThread(int subjectId, ActivityStatus questionStatus) {
		this.subjectId = subjectId;
		this.questionStatus = questionStatus;
	}

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

		String key = QuestionCache.getAnswerLogListKey(subjectId);
		while (true) {

			// 取试卷
			String answerLogStr = CacheUtil.lpop(key);
			if (answerLogStr == null) {
				break;
			}
			System.out.println("取到试卷：answerLogStr=" + answerLogStr);
			AnswerLog answer = JSONObject.parseObject(answerLogStr, AnswerLog.class);

			// 用户答题状态记录
			String accountId = answer.getAccountId();
			AccountActivityStatus accountStatus = QuestionCache.getAccountStatus(accountId);
			
			// 之前没有找到答题记录，那么必须从第一题开始，否则作废
			if (accountStatus == null) {
				if (questionStatus.getQuestionN() == 0) {
					accountStatus = new AccountActivityStatus();
					if (answer.getpAnswer() == rightAnswer) { // 答对
						System.out.println("答对");
						accountStatus.setRAnswer(true);
						accountStatus.setLive(true);
						accountStatus.setCanUsedCard(true);
						accountStatus.setOrder(0);
					} else { // 答错
						System.out.println("答错");
						accountStatus.setRAnswer(false);
						accountStatus.setLive(useCard(accountId, false)); // 根据是否可以使用复活卡，决定生死
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

			/** 之前有答题记录 **/

			// 超过两题未答
			else if (answerSubject.getRealOrder() - accountStatus.getOrder() > 2) {
				accountStatus.setRAnswer(answer.getpAnswer() == rightAnswer);
				accountStatus.setLive(false);
				accountStatus.setCanUsedCard(false);
				logger.info("(连续断网两题才会发生的情况, 如果连续不答题，不会走到这里) accountId={}，当前进行到第{}题，用户只答到第{}题，用户错过了两题以上！", accountId,
						questionStatus.getQuestionN(), accountStatus.getOrder());
			}

			// 有一题未答
			else if (answerSubject.getRealOrder() - accountStatus.getOrder() == 2) {
				accountStatus.setRAnswer(answer.getpAnswer() == rightAnswer);
				accountStatus.setLive(useCard(accountId, accountStatus.isCanUsedCard())); // 使用复活卡
				accountStatus.setCanUsedCard(false);
				accountStatus.setOrder(answerSubject.getRealOrder());
				logger.info("accountId={}，当前进行到第{}题，用户只答到第{}题，用户错过了一题！", accountId, questionStatus.getQuestionN(),
						accountStatus.getOrder());
			}

			// 正常情况挨个答题
			else if (answer.getpAnswer() == rightAnswer) { // 答对
				accountStatus.setRAnswer(true);
				accountStatus.setOrder(answerSubject.getRealOrder());
			} else if (answer.getpAnswer() != rightAnswer) { // 答错
				accountStatus.setRAnswer(false);
				accountStatus.setLive(useCard(accountId, accountStatus.isCanUsedCard()));
				accountStatus.setCanUsedCard(false);
				accountStatus.setOrder(answerSubject.getRealOrder());
			} else {
				logger.error("遇到未考虑的情况， accountId={}", accountId);
			}

			// 更新用户的答题状态
			QuestionCache.updateAccountStatus(accountId, accountStatus);
			
			// 更新答题总数据
			StaticService.updateAnswerData(questionStatus.getQuestionN(), answer.getpAnswer(), accountStatus.isLive()
					&& !accountStatus.isRAnswer(), accountStatus.isLive());
		}

		// 阅卷结束，修改状态
		logger.info("阅卷结束！ ");
		System.out.println("阅卷结束！ " + ", questionStatus=" + JSONObject.toJSONString(questionStatus));
		questionStatus.setQuestionS(1);

		QuestionCache.updateQuestionStatus(answerSubject.getActivityId(), questionStatus);
	}

	/**
	 * 
	 * @param aid
	 *            用户id
	 * @param isCanUsedCard
	 *            能否使用复活卡
	 * @return
	 */
	private boolean useCard(String aid, boolean isCanUsedCard) {
		AccountInfo aInfo = QuestionCache.getAccountInfo(aid);
		if (!isCanUsedCard) {
			logger.info("不能够使用复活卡, accountId={}", aid);
			return false;
		}
		if (aInfo != null && aInfo.getCardNum() > 0) {
			aInfo.reduceOne();
			QuestionCache.updateAccountInfo(aid, aInfo);
			logger.info("使用复活卡重生, accountId={}", aid);
			return true;
		}
		return false;
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
}
