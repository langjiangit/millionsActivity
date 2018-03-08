package com.bus.chelaile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.service.StaticService;

/**
 * 答题数据总结
 * 
 * @author quekunkun
 *
 */
public class AnswerData {

	Logger logger = LoggerFactory.getLogger(AnswerData.class);

	// private int realOrder; // 当前第几题
	private int subjectId;

	private NumbersModel watchLiving = new NumbersModel(); // 在线总人数
	private NumbersModel answerNum = new NumbersModel(); // 可答题人数
	private NumbersModel option1Num = new NumbersModel(); // 第1个选项人数
	private NumbersModel option2Num = new NumbersModel();
	private NumbersModel option3Num = new NumbersModel();
	private NumbersModel notAnswer = new NumbersModel(); // 未答题人数
	private NumbersModel reLive = new NumbersModel(); // 使用复活卡人数

//	private NumbersModel outPeople = new NumbersModel(); // 出局的人数
	
	private NumbersModel rightOptionNum = new NumbersModel(); // 正确答案的人数

	private boolean hasCountRobot = false; // 是否计算过机器人数目
	
	
	/**
	 * 计算机器人数量
	 */
	public void calcuRobots(int activityId, int questionN, int robotMultiple) {

		try {
			String YJkey = QuestionCache.getYJKey(activityId, questionN);
			String YJRobotKey = QuestionCache.getYJRobotKey(activityId, questionN);
			String YJTotalKey = QuestionCache.getYJTotalKey(activityId, questionN);

			int option1R = getInt(CacheUtil.getHashSetValue(YJkey, "1"));
			int option2R = getInt(CacheUtil.getHashSetValue(YJkey, "2"));
			int option3R = getInt(CacheUtil.getHashSetValue(YJkey, "3"));
			int reliveR = getInt(CacheUtil.getHashSetValue(YJkey, "4"));
//			int rightR = getInt(CacheUtil.getHashSetValue(YJkey, "5"));
			int notAnswerR = getInt(CacheUtil.getHashSetValue(YJkey, "-1"));
			int totalAnswerR = option1R + option2R + option3R + notAnswerR;
			
//			int totalAnswerR = reliveR + rightR;
			
//			if (questionN == 0) { // 第一题， 简单的设置可答题的机器人
//				CacheUtil.setHashSetValue(YJRobotKey, "6", totalAnswerR * robotMultiple + "");
//				CacheUtil.setHashSetValue(YJTotalKey, "6", totalAnswerR * (robotMultiple + 1) + "");
//				logger.info("第一题，设置可答题总人数 , answerNum={}", this.answerNum.getTotal().get());
//			}

			// 可答题人数 
			int robotAnswerNum = getInt(CacheUtil.getHashSetValue(YJRobotKey, "6"));
			int totalAnswerNum = robotAnswerNum + totalAnswerR;
			if(questionN == 0) {
				CacheUtil.setHashSetValue(YJkey, "6", totalAnswerR + "");
				CacheUtil.setHashSetValue(YJTotalKey, "6", totalAnswerNum + "");
			}
			
			double A = (double) option1R / (double) totalAnswerR;
			double B = (double) option2R / (double) totalAnswerR;
			double C = (double) option3R / (double) totalAnswerR;
			double D = (double) notAnswerR / (double) totalAnswerR;

			CacheUtil.setHashSetValue(YJRobotKey, "1", Math.round(robotAnswerNum * A) + "");
			CacheUtil.setHashSetValue(YJRobotKey, "2", Math.round(robotAnswerNum * B) + "");
			CacheUtil.setHashSetValue(YJRobotKey, "3", Math.round(robotAnswerNum * C) + "");
			CacheUtil.setHashSetValue(YJRobotKey, "-1", Math.round(robotAnswerNum * D) + "");

			CacheUtil.setHashSetValue(YJTotalKey, "1", Math.round(totalAnswerNum * A) + "");
			CacheUtil.setHashSetValue(YJTotalKey, "2", Math.round(totalAnswerNum * B) + "");
			CacheUtil.setHashSetValue(YJTotalKey, "3", Math.round(totalAnswerNum * C) + "");
			CacheUtil.setHashSetValue(YJTotalKey, "-1", Math.round(totalAnswerNum * D) + "");

			CacheUtil.setHashSetValue(YJRobotKey, "4", reliveR * robotMultiple + "");
			CacheUtil.setHashSetValue(YJTotalKey, "4", reliveR * (robotMultiple + 1) + "");

			// 记录 ‘这一题答对的人的情况’ ,跟随正确选项即可
			Answer_subject subject = StaticService.getSubject(subjectId);
			if (subject.getAnswer() == 0) {
				CacheUtil.setHashSetValue(YJRobotKey, "5", getInt(CacheUtil.getHashSetValue(YJRobotKey, "1")) + "");
				CacheUtil.setHashSetValue(YJTotalKey, "5", getInt(CacheUtil.getHashSetValue(YJTotalKey, "1")) + "");
			} else if (subject.getAnswer() == 1) {
				CacheUtil.setHashSetValue(YJRobotKey, "5", getInt(CacheUtil.getHashSetValue(YJRobotKey, "2")) + "");
				CacheUtil.setHashSetValue(YJTotalKey, "5", getInt(CacheUtil.getHashSetValue(YJTotalKey, "2")) + "");
			} else if (subject.getAnswer() == 2) {
				CacheUtil.setHashSetValue(YJRobotKey, "5", getInt(CacheUtil.getHashSetValue(YJRobotKey, "3")) + "");
				CacheUtil.setHashSetValue(YJTotalKey, "5", getInt(CacheUtil.getHashSetValue(YJTotalKey, "3")) + "");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/*
	 * 从redis读取数据， 创建AnswerData结构体
	 */
	public void createFromRedis(String yJkey, String yJRobotKey, String yJTotalKey) {
		this.option1Num.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "1")));
		this.option1Num.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "1")));
		this.option1Num.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "1")));
		
		this.option2Num.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "2")));
		this.option2Num.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "2")));
		this.option2Num.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "2")));
		
		this.option3Num.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "3")));
		this.option3Num.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "3")));
		this.option3Num.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "3")));
		
		this.rightOptionNum.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "5")));
		this.rightOptionNum.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "5")));
		this.rightOptionNum.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "5")));
		
		this.answerNum.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "6")));
		this.answerNum.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "6")));
		this.answerNum.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "6")));
		
		this.notAnswer.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "-1")));
		this.notAnswer.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "-1")));
		this.notAnswer.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "-1")));
		
		this.reLive.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "4")));
		this.reLive.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "4")));
		this.reLive.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "4")));
		
		this.watchLiving.getRealNum().set(getInt(CacheUtil.getHashSetValue(yJkey, "7")));
		this.watchLiving.getRobotNum().set(getInt(CacheUtil.getHashSetValue(yJRobotKey, "7")));
		this.watchLiving.getTotal().set(getInt(CacheUtil.getHashSetValue(yJTotalKey, "7")));
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

	/**
	 * 创建下一个态的 QuestionData，将在线人数、和可答题人数继承下去，供下一题的初始化使用 既：
	 * copy在线人数，计算可答题人数（答对的人数+使用复活卡的人数)
	 * 
	 * @param answerData
	 */
	public void copyFromLastData(Integer activityId, int questionN) {
		String YJkey = QuestionCache.getYJKey(activityId, questionN);
		String YJRobotKey = QuestionCache.getYJRobotKey(activityId, questionN);
		String YJTotalKey = QuestionCache.getYJTotalKey(activityId, questionN);
		
		String YJkeyNex = QuestionCache.getYJKey(activityId, questionN + 1);
		String YJRobotKeyNex = QuestionCache.getYJRobotKey(activityId, questionN + 1);
		String YJTotalKeyNex = QuestionCache.getYJTotalKey(activityId, questionN + 1);
		
		CacheUtil.setHashSetValue(YJkeyNex, "7", CacheUtil.getHashSetValue(YJkey, "7"));
		CacheUtil.setHashSetValue(YJRobotKeyNex, "7", CacheUtil.getHashSetValue(YJRobotKey, "7"));
		CacheUtil.setHashSetValue(YJTotalKeyNex, "7", CacheUtil.getHashSetValue(YJTotalKey, "7"));
		
		CacheUtil.setHashSetValue(YJkeyNex, "6", (getInt(CacheUtil.getHashSetValue(YJkey, "5")) + getInt(CacheUtil.getHashSetValue(YJkey, "4"))) + "");
		CacheUtil.setHashSetValue(YJRobotKeyNex, "6", (getInt(CacheUtil.getHashSetValue(YJRobotKey, "5")) + getInt(CacheUtil.getHashSetValue(YJRobotKey, "4"))) + "");
		CacheUtil.setHashSetValue(YJTotalKeyNex, "6", (getInt(CacheUtil.getHashSetValue(YJTotalKey, "5")) + getInt(CacheUtil.getHashSetValue(YJTotalKey, "4"))) + "");
		
	}


	/*
	 * 来自运营后台的###数据修正###
	 */
	public void changeData(int rightAnswer, int watchingRobot, int option1Robot, int option2Robot, int option3Robot, int notAnsRobot,
			int reLivingRobot, int activityId, int questionN) {
		int rightRobot = 0;
		String YJKey = QuestionCache.getYJKey(activityId, questionN);
		String YJRobotKey = QuestionCache.getYJRobotKey(activityId, questionN);
		String YJTotalKey = QuestionCache.getYJTotalKey(activityId, questionN);
		
		if (watchingRobot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "7", watchingRobot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "7", (watchingRobot + getInt(CacheUtil.getHashSetValue(YJKey, "7"))) + "");
		}
		if (option1Robot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "1", option1Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "1", (option1Robot + getInt(CacheUtil.getHashSetValue(YJKey, "1"))) + "");
			if(rightAnswer == 0)
				rightRobot = option1Robot;
		}
		if (option2Robot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "2", option2Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "2", (option2Robot + getInt(CacheUtil.getHashSetValue(YJKey, "2"))) + "");
			if(rightAnswer == 1)
				rightRobot = option2Robot;
		}
		if (option3Robot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "3", option3Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "3", (option3Robot + getInt(CacheUtil.getHashSetValue(YJKey, "3"))) + "");
			if(rightAnswer == 2)
				rightRobot = option3Robot;
		}
		if (notAnsRobot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "-1", notAnsRobot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "-1", (notAnsRobot + getInt(CacheUtil.getHashSetValue(YJKey, "-1"))) + "");
		}
		
		
		if (reLivingRobot != -1) {
			CacheUtil.setHashSetValue(YJRobotKey, "4", reLivingRobot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "4", (reLivingRobot + getInt(CacheUtil.getHashSetValue(YJKey, "4"))) + "");
		
			// 可答题人数，直接受复活人数+ right answer people
			CacheUtil.setHashSetValue(YJRobotKey, "6", (reLivingRobot + rightRobot) + "");
			CacheUtil.setHashSetValue(YJTotalKey, "6", (reLivingRobot + getInt(CacheUtil.getHashSetValue(YJKey, "4"))
					+ rightRobot + getInt(CacheUtil.getHashSetValue(YJKey, "5"))) + "");
		}
		
		
		if(rightAnswer == 0) {
			CacheUtil.setHashSetValue(YJRobotKey, "5", option1Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "5", (option1Robot + getInt(CacheUtil.getHashSetValue(YJKey, "1"))) + "");
		} else if(rightAnswer == 1) {
			CacheUtil.setHashSetValue(YJRobotKey, "5", option2Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "5", (option2Robot + getInt(CacheUtil.getHashSetValue(YJKey, "2"))) + "");
		} else if(rightAnswer == 2) {
			CacheUtil.setHashSetValue(YJRobotKey, "5", option3Robot + "");
			CacheUtil.setHashSetValue(YJTotalKey, "5", (option3Robot + getInt(CacheUtil.getHashSetValue(YJKey, "3"))) + "");
		}
		
		// 将修改复制到下一题的初始化数据中
		copyFromLastData(activityId, questionN);
	}

	public NumbersModel getWatchLiving() {
		return watchLiving;
	}

	public void setWatchLiving(NumbersModel watchLiving) {
		this.watchLiving = watchLiving;
	}

	public NumbersModel getAnswerNum() {
		return answerNum;
	}

	public void setAnswerNum(NumbersModel answerNum) {
		this.answerNum = answerNum;
	}

	public NumbersModel getOption1Num() {
		return option1Num;
	}

	public void setOption1Num(NumbersModel option1Num) {
		this.option1Num = option1Num;
	}

	public NumbersModel getOption2Num() {
		return option2Num;
	}

	public void setOption2Num(NumbersModel option2Num) {
		this.option2Num = option2Num;
	}

	public NumbersModel getOption3Num() {
		return option3Num;
	}

	public void setOption3Num(NumbersModel option3Num) {
		this.option3Num = option3Num;
	}

	public NumbersModel getNotAnswer() {
		return notAnswer;
	}

	public void setNotAnswer(NumbersModel notAnswer) {
		this.notAnswer = notAnswer;
	}

	public NumbersModel getReLive() {
		return reLive;
	}

	public void setReLive(NumbersModel reLive) {
		this.reLive = reLive;
	}

	public boolean isHasCountRobot() {
		return hasCountRobot;
	}

	public void setHasCountRobot(boolean hasCountRobot) {
		this.hasCountRobot = hasCountRobot;
	}

//	public NumbersModel getOutPeople() {
//		return outPeople;
//	}
//
//	public void setOutPeople(NumbersModel outPeople) {
//		this.outPeople = outPeople;
//	}

	// public int getRealOrder() {
	// return realOrder;
	// }
	//
	// public void setRealOrder(int realOrder) {
	// this.realOrder = realOrder;
	// }

	public static void main(String[] args) {
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public NumbersModel getRightOptionNum() {
		return rightOptionNum;
	}

	public void setRightOptionNum(NumbersModel rightOptionNum) {
		this.rightOptionNum = rightOptionNum;
	}
}
