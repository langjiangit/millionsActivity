package com.bus.chelaile.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
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

	private NumbersModel outPeople = new NumbersModel(); // 出局的人数
	
	private NumbersModel rightOptionNum = new NumbersModel(); // 正确答案的人数

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
			break;
		default:
			this.notAnswer.getRealNum().getAndAdd(1);
			break;
		}
		if (hasUsedCard) {
			this.reLive.getRealNum().getAndAdd(1);
		}
		if (!isLive) {
			this.outPeople.getRealNum().getAndAdd(1);
		}
//		if (isLive && !hasUsedCard) {  // 或者，没有使用复活卡，所以--> 答对
//			this.getRightOptionNum().getRealNum().getAndAdd(1);
//		}
	}

	/**
	 * 计算机器人数量
	 */
	public void calcuRobots(int questionN, int robotMultiple) {

		// if (questionN == 0) {
		int totalAnswerNum = this.answerNum.getTotal().get(); // 总‘可答题人数’
		int realAnswerNum = this.answerNum.getRealNum().get(); // 真实的‘可答题人数’，第一题开题前=在线真实人数
		int robotAnswerNum = this.answerNum.getRobotNum().get();
		int realOutNum = this.outPeople.getRealNum().get(); // ‘已知的’出局的人数

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
		this.option3Num.getRobotNum().set((int) (robotAnswerNum * C));
		this.notAnswer.getRobotNum().set((int) (robotAnswerNum * D));
		this.outPeople.getRobotNum().set((int) (robotAnswerNum * E));

		this.option1Num.getTotal().set((int) (totalAnswerNum * A));
		this.option2Num.getTotal().set((int) (totalAnswerNum * B));
		this.option3Num.getTotal().set((int) (totalAnswerNum * C));
		this.notAnswer.getTotal().set((int) (totalAnswerNum * D));
		this.outPeople.getTotal().set((int) (totalAnswerNum * E));

		this.notAnswer.getRealNum().set(realNoAnswer);

		this.reLive.getRobotNum().set(this.reLive.getRealNum().get() * robotMultiple);
		this.reLive.getTotal().set(this.reLive.getRobotNum().get() + this.reLive.getRealNum().get());

		// 记录 ‘这一题答对的人的情况’ ,跟随正确选项即可 
		this.setSubjectId(StaticService.ORDER_SUBJECT.get(questionN));
		Answer_subject subject = StaticService.getSubject(subjectId);
		if(subject.getAnswer() == 0) {
			this.rightOptionNum.getTotal().set(this.option1Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option1Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option1Num.getRobotNum().get());
		} else if(subject.getAnswer() == 1) {
			this.rightOptionNum.getTotal().set(this.option2Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option2Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option2Num.getRobotNum().get());
		} else if(subject.getAnswer() == 2) {
			this.rightOptionNum.getTotal().set(this.option3Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option3Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option3Num.getRobotNum().get());
		}

	}

	/**
	 * 创建下一个态的 QuestionData，将在线人数、和可答题人数继承下去，供下一题的初始化使用 既：
	 * copy在线人数，计算可答题人数（答对的人数+使用复活卡的人数)
	 * 
	 * @param answerData
	 */
	public void copyFromLastData(AnswerData answerData) {
		this.watchLiving = answerData.watchLiving;

		this.answerNum.getRealNum().set(
				answerData.getRightOptionNum().getRealNum().get() + answerData.getReLive().getRealNum().get());
		this.answerNum.getRobotNum().set(
				answerData.getRightOptionNum().getRobotNum().get() + answerData.getReLive().getRobotNum().get());
		this.answerNum.getTotal().set(
				answerData.getRightOptionNum().getTotal().get() + answerData.getReLive().getTotal().get());
	}

	/**
	 * 更新在线人数
	 * 
	 * @param realLive
	 */
	// 这个工作，拿出来用单独的线程跑，感觉会更好
	// TODO 
	public void updateWatchLiving(int realLive, int robotMultiple) {
		int lastRealLive = this.getWatchLiving().getRealNum().get();
		int lastRobotLive = this.getWatchLiving().getRobotNum().get();
		
		if(realLive > lastRealLive) {
			// 人数增多
			int add = (int) Math.round((realLive - lastRealLive) * (0.5 + Math.random()) * robotMultiple);
			this.getWatchLiving().getRobotNum().set(lastRobotLive + add);
		} else {
			// 人数降低
			int sub = (int) Math.round((lastRealLive - realLive) * (0.2 + Math.random() * 0.3) * robotMultiple);
			this.getWatchLiving().getRobotNum().set(lastRobotLive - sub);
		}
		  
		this.getWatchLiving().getTotal().set(realLive + this.getWatchLiving().getRobotNum().get());
		this.getWatchLiving().getRealNum().set(realLive);
	}

	
	/*
	 * 更新可答题人数
	 */
	public void copyAnswerFromWatch() {
		int realLive = this.getWatchLiving().getRealNum().get();
		int robotLive = this.getWatchLiving().getRobotNum().get();
		int totalLive = this.getWatchLiving().getTotal().get();
		
		this.answerNum.getRealNum().set(realLive);
		this.answerNum.getTotal().set(totalLive);
		this.answerNum.getRobotNum().set(robotLive);
	}

	/*
	 * 来自运营后台的###数据修正###
	 */
	public void changeData(int rightAnswer, int watchingRobot, int option1Robot, int option2Robot, int option3Robot, int notAnsRobot,
			int reLivingRobot) {
		int rightRobot = 0;
		
		if (watchingRobot != -1) {
			this.watchLiving.getRobotNum().set(this.watchLiving.getRobotNum().get() + watchingRobot);
			this.watchLiving.getTotal().set(this.watchLiving.getTotal().get() + watchingRobot);
		}
		if (option1Robot != -1) {
			this.option1Num.getRobotNum().set(this.option1Num.getRobotNum().get() + option1Robot);
			this.option1Num.getTotal().set(this.option1Num.getTotal().get() + option1Robot);
			if(rightAnswer == 0)
				rightRobot = option1Robot;
		}
		if (option2Robot != -1) {
			this.option2Num.getRobotNum().set(this.option2Num.getRobotNum().get() + option2Robot);
			this.option2Num.getTotal().set(this.option2Num.getTotal().get() + option2Robot);
			if(rightAnswer == 1)
				rightRobot = option2Robot;
		}
		if (option3Robot != -1) {
			this.option3Num.getRobotNum().set(this.option3Num.getRobotNum().get() + option3Robot);
			this.option3Num.getTotal().set(this.option3Num.getTotal().get() + option3Robot);
			if(rightAnswer == 2)
				rightRobot = option3Robot;
		}
		if (notAnsRobot != -1) {
			this.notAnswer.getRobotNum().set(this.notAnswer.getRobotNum().get() + notAnsRobot);
			this.notAnswer.getTotal().set(this.notAnswer.getTotal().get() + notAnsRobot);
		}
		
		
		if (reLivingRobot != -1) {
			this.reLive.getRobotNum().set(this.reLive.getRobotNum().get() + reLivingRobot);
			this.reLive.getTotal().set(this.reLive.getTotal().get() + reLivingRobot);
		
			// 可答题人数，直接受复活人数+ right answer people
			this.answerNum.getRobotNum().set(this.answerNum.getRobotNum().get() + reLivingRobot + rightRobot);
			this.answerNum.getTotal().set(this.answerNum.getTotal().get() + reLivingRobot + rightRobot);
		}
		
		
		if(rightAnswer == 0) {
			this.rightOptionNum.getTotal().set(this.option1Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option1Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option1Num.getRobotNum().get());
		} else if(rightAnswer == 1) {
			this.rightOptionNum.getTotal().set(this.option2Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option2Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option2Num.getRobotNum().get());
		} else if(rightAnswer == 2) {
			this.rightOptionNum.getTotal().set(this.option3Num.getTotal().get());
			this.rightOptionNum.getRealNum().set(this.option3Num.getRealNum().get());
			this.rightOptionNum.getRobotNum().set(this.option3Num.getRobotNum().get());
		}
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

	public NumbersModel getOutPeople() {
		return outPeople;
	}

	public void setOutPeople(NumbersModel outPeople) {
		this.outPeople = outPeople;
	}

	// public int getRealOrder() {
	// return realOrder;
	// }
	//
	// public void setRealOrder(int realOrder) {
	// this.realOrder = realOrder;
	// }

	public static void main(String[] args) {
		AnswerData data = new AnswerData();
		data.updateWatchLiving(12, 20);

		System.out.println(JSONObject.toJSONString(data));
		
		System.out.println((int) Math.round(10 * (0.5 + Math.random()) * 10));
		System.out.println((int) Math.round(20 * (0.5 + Math.random()) * 10));
		System.out.println((int) Math.round(10 * (0.5 + Math.random()) * 10));
		System.out.println((int) Math.round(100 * (0.5 + Math.random()) * 10));
		
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
