package com.bus.chelaile.model.pubmsg;

import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.Answer_subject;


/**
 * 下发的题目
 * @author quekunkun
 *
 */
public class PubQuestion {

	private int k; // 题目的key
	private int n; // 题目需要，当前活动的第几题
	private String t; // 问题
	private String c; // 选项内容

	public PubQuestion() {
		super();
	}

	/*
	 * 构建下发的题目
	 */
	public PubQuestion(ActivityStatus questionStatus, Answer_subject question) {
		super();
		this.k = question.getId();
		this.t = question.getSubject();
		this.c = question.getOption();
		
		this.n = questionStatus.getQuestionN() + 1;
	}

	
	public int getK() {
		return k;
	}

	public void setK(int k) {
		this.k = k;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}

	public String getT() {
		return t;
	}

	public void setT(String t) {
		this.t = t;
	}

	public String getC() {
		return c;
	}

	public void setC(String c) {
		this.c = c;
	}
}
