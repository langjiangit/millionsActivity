package com.bus.chelaile.model;

/**
 * 活动进行到哪个阶段了
 * @author quekunkun
 *
 */
public class ActivityStatus {

	private int questionN;	// 第几道题 ，0、1、2……
	private int questionS;	// 题目状态， -1， 未发题； 0 发题了； 1  阅卷结束；2 计算完毕了；3 发布答案了
	
	public ActivityStatus() {
		super();
		this.questionN = -1;
		this.questionS = -1;
	}
	
	public int getQuestionN() {
		return questionN;
	}
	public void setQuestionN(int questionN) {
		this.questionN = questionN;
	}
	public int getQuestionS() {
		return questionS;
	}
	public void setQuestionS(int questionS) {
		this.questionS = questionS;
	}

	
	// 更新状态
	public void addNumber() {
		this.questionN ++ ;
	}
	
	public void addStatus() {
		this.questionS ++ ;
	}

	/*
	 * 是否还未开始第一道答题
	 */
	public boolean isNotBegin() {
		return (this.questionN == 0) && (this.questionS == -1);
	}

	/*
	 * 计算
	 */
	public boolean hasToCalculated() {
		return(this.questionS == 1);
	}
	
	public boolean hasCalculated() {
		return(this.questionS == 2);
	}
}
