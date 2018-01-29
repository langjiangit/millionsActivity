package com.bus.chelaile.model;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.mvc.QuestionParam;

/**
 * 用户答题log（答卷）
 * @author quekunkun
 *
 */
public class AnswerLog {

	private String accountId; // 用户注册id
	private int questionId;	// 题目id
	private int pAnswer; // 用户答案  0 \ 1 \ 2
	private long pTime; //答题时间
	
	public AnswerLog() {
		super();
	}


//	public AnswerLog(String accountId, int questionId, int pAnswer, long pTime) {
//		super();
//		this.accountId = accountId;
//		this.questionId = questionId;
//		this.pAnswer = pAnswer;
//		this.pTime = pTime;
//	}


	public AnswerLog(QuestionParam param) {
		this.accountId = param.getAccountId();
		this.questionId = param.getSubjectId();
		this.pAnswer = param.getAnswer();
		this.pTime = System.currentTimeMillis();
	}
	
	
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public int getpAnswer() {
		return pAnswer;
	}
	public void setpAnswer(int pAnswer) {
		this.pAnswer = pAnswer;
	}
	public long getpTime() {
		return pTime;
	}
	public void setpTime(long pTime) {
		this.pTime = pTime;
	}
	
	public static void main(String[] args) {
		String a = "{\"accountId\":\"123\",\"pAnswer\":2,\"pTime\":1517038576713,\"questionId\":1}";
//		AnswerLog an = JSONObject.toJavaObject(a, AnswerLog.class);
//		System.out.println(an.getAccountId());
		
		AnswerLog en = JSONObject.parseObject(a, AnswerLog.class);
		System.out.println(en.getAccountId());
	}
}
