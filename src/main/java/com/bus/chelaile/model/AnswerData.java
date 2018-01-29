package com.bus.chelaile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;


/**
 * 答题数据总结
 * 
 * @author quekunkun
 *
 */
public class AnswerData {

	Logger logger = LoggerFactory.getLogger(AnswerData.class);

//	private int realOrder;	 // 当前第几题
	private String subjectId;

	private NumbersModel watchLiving = new NumbersModel(); // 在线总人数
	private NumbersModel answerNum = new NumbersModel(); // 可答题人数
	private NumbersModel option1Num = new NumbersModel(); // 第1个选项人数
	private NumbersModel option2Num = new NumbersModel();
	private NumbersModel option3Num = new NumbersModel();
	private NumbersModel notAnswer = new NumbersModel(); // 未答题人数
	private NumbersModel reLive = new NumbersModel(); // 使用复活卡人数

	private NumbersModel outPeople = new NumbersModel(); // 出局的人数

	private boolean hasCountRobot = false; // 是否计算过机器人数目

	/*
	 * 阅卷中，增加每个选项的真实人数
	 */
	public void addOneRecord(int questionStatusN, int pAnswer, boolean hasUsedCard, boolean isLive) {

		switch (pAnswer) {
		case 0:
			this.option1Num.getRealNum().getAndAdd(1);
			break;
		case 1:
			this.option2Num.getRealNum().getAndAdd(1);
			break;
		case 2:
			this.option3Num.getRealNum().getAndAdd(1);
		default:
			break;
		}
		if (hasUsedCard) {
			this.reLive.getRealNum().getAndAdd(1);
		}
		if (!isLive) {
			this.outPeople.getRealNum().getAndAdd(1);
		}
	}

	/**
	 * 计算机器人数量
	 */
	public void calcuRobots(int questionN, int robotMultiple) {

//		if (questionN == 0) {
			int totalAnswerNum = this.answerNum.getTotal().get(); // 总‘可答题人数’
			int realAnswerNum = this.answerNum.getRealNum().get(); // 真实的‘可答题人数’，第一题开题前=在线真实人数
			int robotAnswerNum = this.answerNum.getRobotNum().get();
			int realOutNum = this.outPeople.getRealNum().get(); // 出局的人数

			int realOptions = this.option1Num.getRealNum().get() + this.option2Num.getRealNum().get()
					+ this.option3Num.getRealNum().get(); // 选择了答案的总 real 人数
			int realNoAnswer = realAnswerNum - realOptions; // 未答题 real 人数

			// 第一题，可答题人数等于在线人数
			// A + B + C + D = 1.0
			double A = (double) this.option1Num.getRealNum().get() / (double) realAnswerNum;
			double B = (double) this.option2Num.getRealNum().get() / (double) realAnswerNum;
			double C = (double) this.option3Num.getRealNum().get() / (double) realAnswerNum;
			double D = (double) realNoAnswer / (double) realAnswerNum;

			double E = (double) realOutNum / (double) realAnswerNum; // 出局人数占比,用来同步出局机器人和出局总人数
			// this.answerNum.getRealNum().set((this.watchLiving.getRealNum().get()));
			// this.answerNum.getRobotNum().set((this.watchLiving.getRobotNum().get()));
			// this.answerNum.getTotal().set((this.watchLiving.getTotal().get()));
			this.option1Num.getRobotNum().set((int) (robotAnswerNum * A));
			this.option2Num.getRobotNum().set((int) (robotAnswerNum * B));
			this.option2Num.getRobotNum().set((int) (robotAnswerNum * C));
			this.notAnswer.getRobotNum().set((int) (robotAnswerNum * D));
			this.outPeople.getRobotNum().set((int) (robotAnswerNum * E));

			this.option1Num.getTotal().set((int) (totalAnswerNum * A));
			this.option2Num.getTotal().set((int) (totalAnswerNum * B));
			this.option3Num.getTotal().set((int) (totalAnswerNum * C));
			this.notAnswer.getTotal().set((int) (totalAnswerNum * D));
			this.outPeople.getTotal().set((int) (totalAnswerNum * E));

			this.notAnswer.getRealNum().set(realNoAnswer);

			int robotMulti = 100; // TODO
			this.reLive.getRobotNum().set(this.reLive.getRealNum().get() * robotMulti);
			this.reLive.getTotal().set(this.reLive.getRobotNum().get() + this.reLive.getRealNum().get());
			//		} 
//		else {
//
//			AnswerData lastAnswerData = StaticService.getAnswerData(questionN - 1);
//			NumbersModel lastOutPeople = lastAnswerData.getOutPeople();
//			logger.info("上一题的计算结果为：lastAnswerData={}", JSONObject.toJSONString(lastAnswerData));
//
//			
//			int totalAnswerNum = this.answerNum.getTotal().get(); // 总‘可答题人数’
//			int realAnswerNum = this.answerNum.getRealNum().get(); // 真实的‘可答题人数’，第一题开题前=在线真实人数
//			int robotAnswerNum = this.answerNum.getRobotNum().get();
//			int realOutNum = this.outPeople.getRealNum().get(); // 出局的人数
//
//			int realOptions = this.option1Num.getRealNum().get() + this.option2Num.getRealNum().get()
//					+ this.option3Num.getRealNum().get(); // 选择了答案的总 real 人数
//			int realNoAnswer = realAnswerNum - realOptions; // 未答题 real 人数
//			
//			
//		}
	}
	
	
	/**
	 * 创建下一个态的 QuestionData，将在线人数、和可答题人数继承下去，供下一题的初始化使用
	 * 既： copy在线人数，计算可答题人数（当前可答题人数-out的人数)
	 * @param answerData
	 */
	public void copyFromLastData(AnswerData answerData) {
		
		this.watchLiving = answerData.watchLiving;
		
		this.answerNum.getRealNum().set(answerData.getAnswerNum().getRealNum().get() - answerData.getOutPeople().getRealNum().get());
		this.answerNum.getRobotNum().set(answerData.getAnswerNum().getRobotNum().get() - answerData.getOutPeople().getRobotNum().get());
		this.answerNum.getTotal().set(answerData.getAnswerNum().getTotal().get() - answerData.getOutPeople().getTotal().get());
	}

	

	public void updateWatchLiving(int realLive) {
		// TODO Auto-generated method stub
		this.getWatchLiving().getTotal().set(10001);
		this.getWatchLiving().getRealNum().set(111);
		this.getWatchLiving().getRobotNum().set(10001 - 111);
		
		
	}

	public void updateAnswerNum(int realAnswerNum) {
		// TODO Auto-generated method stub
		this.getAnswerNum().getTotal().set(10001);
		this.getAnswerNum().getRealNum().set(111);
		this.getAnswerNum().getRobotNum().set(10001 - 111);
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

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public boolean isHasCountRobot() {
		return hasCountRobot;
	}

	public void setHasCountRobot(boolean hasCountRobot) {
		this.hasCountRobot = hasCountRobot;
	}

	public NumbersModel getOutPeople() {
		return outPeople;
	}

	public void setOutPeople(NumbersModel outPeople) {
		this.outPeople = outPeople;
	}

//	public int getRealOrder() {
//		return realOrder;
//	}
//
//	public void setRealOrder(int realOrder) {
//		this.realOrder = realOrder;
//	}
	
	public static void main(String[] args) {
		AnswerData data = new AnswerData();
		data.updateWatchLiving(12);
		data.updateAnswerNum(123);
		
		System.out.println(JSONObject.toJSONString(data));
	}
}
