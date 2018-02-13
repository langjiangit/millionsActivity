package com.bus.chelaile.thread;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerLog;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.service.StaticService;
import com.bus.chelaile.util.New;

public class CalculateAnswerThread implements Runnable {
	private int subjectId;
	private ActivityStatus questionStatus;
	private int activityId;
    private CountDownLatch cntLatch;
    int                    num    = -1;
//    private Map<String, String> allAnswers;
	Logger logger = LoggerFactory.getLogger(CalculateAnswerThread.class);

	public CalculateAnswerThread(int subjectId, ActivityStatus questionStatus, int activityId,int num,
			CountDownLatch cntLatch) {
		this.subjectId = subjectId;
		this.questionStatus = questionStatus;
		this.activityId = activityId;
		this.num=num;
		this.cntLatch=cntLatch;
//		this.allAnswers = allAnswers;
	}

	/**
	 * 处理提交了当前题目答案的用户
	 * 需要提前校验用户之前的答题状况
	 */
	@Override
	public void run() {
		logger.info("开始阅卷！ , questionStatus={}", JSONObject.toJSONString(questionStatus));
		int qN = questionStatus.getQuestionN();
		Answer_subject answerSubject = StaticService.getSubject(subjectId);
		int rightAnswer = answerSubject.getAnswer();
		
//		String livePeopleKey = QuestionCache.getLivePeoPleKey(activityId, qN);
		String DTkey = QuestionCache.getAnswerLogListKey(qN, activityId);
//		Map<String, String> allAnswers = CacheUtil.getHsetAll(DTkey);
		
		// field ，  1 选1人数， 2 选2人数， 3选3人数， 4 使用复活卡人数， 5 选正确人数， -1 未作答人数
		String YJkey = QuestionCache.getYJKey(activityId, qN);
		// 存活
		String CHKey = QuestionCache.getLiveKey(activityId, qN);
		
		String dtPersonK = QuestionCache.getPeopleJJKey(activityId, qN);
		
		try {
            while (true) {
            	String accountId = null;
            	try {
            		accountId = CacheUtil.lpop(dtPersonK);
            		if (accountId == null)
            			break;
            	} catch (Exception e) {
            		logger.error("pop error ", e);
            		continue;
            	}
//            	long tb = System.currentTimeMillis();
            	
            	try {
//            		String answerLogStr = allAnswers.get(accountId);
            		String answerLogStr = CacheUtil.getHashSetValue(DTkey, accountId);
            		if (answerLogStr == null) {
            			logger.error("answerLogStr为空 ");
            			continue;
            		}
            		AnswerLog answer = JSONObject.parseObject(answerLogStr, AnswerLog.class);
//            		logger.info("accountId={},  costTime1={}", accountId, System.currentTimeMillis() - tb);
            		
            		// 超时判断
					Answer_subject subject = StaticService.getSubject(subjectId);
					long startTime = subject.getEffectStartTime();
					long endTime = subject.getEffectEndTime();
					if (answer.getpTime() > endTime || answer.getpTime() < startTime) {
						logger.info("提交答案超时， accountId={}, subjectId={}", accountId, subjectId);
						continue;
					}

            		String answerK = QuestionCache.getAnswerKey(accountId, answer.getQuestionId());
            		if (CacheUtil.incrToCache(answerK, -1) != 1) {
            			logger.info("重复提交答案，拒绝入队列, accountId={}, subjectId={}", accountId, answer.getQuestionId());
            			continue;
            		}
//            		logger.info("accountId={},  costTime2={}", accountId, System.currentTimeMillis() - tb);
            		// -11 未答，死了, -1 答错，死了,
            		// 0 未知(做活着处理)
            		// 1 答对，活着, 2 答错，活者 (使用复活卡), 12 未答，活着 (使用复活卡)
            		int userStatus = 0;
            		String UDTKey = QuestionCache.getAccountAnswerLogKey(activityId, accountId);
            		boolean canUseCard = canUsedCard(UDTKey, accountId);
//            		logger.info("accountId={},  costTime3={}", accountId, System.currentTimeMillis() - tb);
            		
            		// 阅卷前判断用户是否有资格
            		String lastUserStatus = CacheUtil.getHashSetValue(UDTKey, String.valueOf(qN - 1));
            		if(lastUserStatus == null) {
            			if(qN > 0) {
//            				logger.info("用户没有资格继续答题:上一题UDT状态为空, accountId{}, qN={}, lastUserSatus={}", accountId, qN, lastUserStatus);
            				continue;
            			}
            		} else {
            			if(getInt(lastUserStatus) < 0) {
//            				logger.info("用户没有资格继续答题:上一题UDT状态为不可继续答题, accountId{}, qN={}, lastUserSatus={}", accountId, qN, lastUserStatus);
            				continue;
            			}
            		}

            		if (answer.getpAnswer() == rightAnswer) { // 答对
            			userStatus = 1;
            			CacheUtil.addHashSetValue(YJkey, "5", 1);
            		} else { // 答错
//            			logger.info("答错， accountId={}", answer.getAccountId());
            			if (QuestionCache.useCard(accountId, canUseCard, true)) {
            				userStatus = 2;
            				CacheUtil.addHashSetValue(YJkey, "4", 1);
            				CacheUtil.setHashSetValue(UDTKey, "-11", "1"); // 用户使用复活卡
            			} else {
            				userStatus = -1;
            			}
            		}
//            		logger.info("accountId={},  costTime4={}", accountId, System.currentTimeMillis() - tb);
            		switch (answer.getpAnswer()) {
            		case 0:
            			CacheUtil.addHashSetValue(YJkey, "1", 1);
            			break;
            		case 1:
            			CacheUtil.addHashSetValue(YJkey, "2", 1);
            			break;
            		case 2:
            			CacheUtil.addHashSetValue(YJkey, "3", 1);
            			break;
            		default:
            			break;
            		}

            		if (userStatus > 0) {
            			CacheUtil.saddMembers(CHKey, accountId);
            		}
            		CacheUtil.setHashSetValue(UDTKey, String.valueOf(qN), String.valueOf(userStatus));

            	} catch (Exception e) {
            		logger.error("阅卷出错 ", e);
            		continue;
            	}
//            	logger.info("accountId={},  costTime={}", accountId, System.currentTimeMillis() - tb);
            }
        } catch (Exception e1) {
            logger.error("CalculateAnswerThread.while(true)", e1);
        }finally{
            logger.info("活动{} 题目一{} num{} countDown",activityId,qN,num);
            cntLatch.countDown();
        }
		
		 //如果线程0任务wait到这里
        if (num == 0) {
            try {
                logger.info("活动{} 题目一{} num{} await",activityId,qN,num);
                // 等待所有线程计算完毕
                cntLatch.await();
            } catch (InterruptedException e) {
                logger.error("cntLatch.await ", e);
            }
            // 不是第一题，需要计算出‘未答题的人’
            if (qN > 0) {
                String lastLiveKey = QuestionCache.getLiveKey(activityId, qN - 1);
                Set<String> lastLiveP = CacheUtil.getSet(lastLiveKey);
                Set<String> notAnswers = New.hashSet();
                notAnswers.addAll(lastLiveP);
//                notAnswers.removeAll(allAnswers.keySet());
                notAnswers.removeAll(CacheUtil.getHKeys(DTkey));
                logger.info("获取到未作答人数, questionN={}, notanswerPN={}", qN, notAnswers.size());

                for (String aId : notAnswers) {
                    String UDTKey = QuestionCache.getAccountAnswerLogKey(activityId, aId);
                    boolean canUseCard = canUsedCard(UDTKey, aId);
                    CacheUtil.addHashSetValue(YJkey, "-1", 1); // 增加未作答人数
                    if (QuestionCache.useCard(aId, canUseCard, true)) {
                        CacheUtil.addHashSetValue(YJkey, "4", 1);
                        CacheUtil.setHashSetValue(UDTKey, "-11", "1"); // 用户使用复活卡
                        CacheUtil.setHashSetValue(UDTKey, String.valueOf(qN), "12");
                        CacheUtil.saddMembers(CHKey, aId);
                    } else {
                        CacheUtil.setHashSetValue(UDTKey, String.valueOf(qN), "-11");
                    }
                }
            }
            
            logger.info("阅卷结束最终！,  questionStatus={}", JSONObject.toJSONString(questionStatus));
            questionStatus.setQuestionS(1);
            QuestionCache.updateQuestionStatus(answerSubject.getActivityId(), questionStatus);
        }
        logger.info("单个线程阅卷结束！ ");

	}

	// 判断本场活动，是否使用过复活卡
	private boolean canUsedCard(String UDTKey, String aId) {
		boolean canUseCard = true;
		String hasUsedCard = CacheUtil.getHashSetValue(UDTKey, "-11");
		if(hasUsedCard != null)	 // 使用过复活卡
			canUseCard = false;
		return canUseCard;
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
}
