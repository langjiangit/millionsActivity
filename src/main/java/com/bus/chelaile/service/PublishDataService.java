package com.bus.chelaile.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerData;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.model.pubmsg.PubAnswer;
import com.bus.chelaile.model.pubmsg.PubCustomInfo;
import com.bus.chelaile.model.pubmsg.PubOptions;
import com.bus.chelaile.model.pubmsg.UserInfo;
import com.bus.chelaile.util.New;

public class PublishDataService {

	protected static final Logger logger = LoggerFactory.getLogger(PublishDataService.class);
	
	/*
	 * 发布答案
	 */
	public void sendAnswer(AnswerData answerData, Answer_subject question, ActivityStatus questionStatus) {

		PubAnswer pubAnswer = new PubAnswer();

		pubAnswer.setK(question.getId());
		pubAnswer.setN(questionStatus.getQuestionN());
		pubAnswer.setT(question.getSubject());
		pubAnswer.setA(question.getAnswer());

		int total = answerData.getAnswerNum().getTotal().get();
		logger.info("发送答案，当前答题进行到第{}题，  总可答题人数为：{}", questionStatus.getQuestionN(), total);
		
		int relive = answerData.getReLive().getTotal().get();
		String numberDesc = numberDesc(relive);
		pubAnswer.setR(numberDesc);

		List<String> options = JSONObject.parseArray(question.getOption(), String.class);
		PubOptions A = new PubOptions(options.get(0), numberDesc(answerData.getOption1Num().getTotal().get()),
				(double) answerData.getOption1Num().getTotal().get() / total);
		PubOptions B = new PubOptions(options.get(1), numberDesc(answerData.getOption2Num().getTotal().get()),
				(double) answerData.getOption2Num().getTotal().get() / total);
		PubOptions C = new PubOptions(options.get(2), numberDesc(answerData.getOption3Num().getTotal().get()),
				(double) answerData.getOption3Num().getTotal().get() / total);

		List<PubOptions> pubOptions = New.arrayList();
		pubOptions.add(A);
		pubOptions.add(B);
		pubOptions.add(C);
		pubAnswer.setC(pubOptions);

		Map<String, PubAnswer> sendAnswer = New.hashMap();
		sendAnswer.put("a", pubAnswer);

		// 发布答案, 更新状态
		CacheUtil.publish("ws", JSONObject.toJSONString(sendAnswer));
		questionStatus.setQuestionS(3);
		QuestionCache.updateQuestionStatus(question.getActivityId(), questionStatus);
	}

	/**
	 * 推送开场倒计时
	 */
	public String sendBeginMsg() {
		Map<String, Integer> beginMsg = New.hashMap();
		beginMsg.put("start", 5);
		CacheUtil.publish("ws", JSONObject.toJSONString(beginMsg));
		
		return "";
	}

	/**
	 * 推送主持人说的话
	 * @param msgType
	 * @param content
	 */
	public String sendEmceeMsg(int msgType, String content) {
		Map<String, JSONObject> emceeMsg = New.hashMap();
		JSONObject msg = new JSONObject();
		msg.put("t", msgType);
		msg.put("c", content);

		emceeMsg.put("m", msg);
		CacheUtil.publish("ws", JSONObject.toJSONString(emceeMsg));
		
		return "";
	}
	
	/**
	 * 推送 通关消息
	 */
	public String sendResult() {
		
		PubCustomInfo puaCustomInfo = new PubCustomInfo();
		puaCustomInfo.setU(20);
		puaCustomInfo.setM("5.90");
		
		UserInfo user = new UserInfo();
		user.setImg("https://image3.chelaile.net.cn/4e3f113d687e4567862b1d0b0384ecc8");
		user.setName("猫咪");
		puaCustomInfo.getuL().add(user);
		
		Map<String, PubCustomInfo> pubResult = New.hashMap();
		pubResult.put("over", puaCustomInfo);
		CacheUtil.publish("ws", JSONObject.toJSONString(pubResult));
		return "";
	}

	private String numberDesc(int relive) {
		String numberDesc;
		if (relive >= 10000) {
			double d = (double) relive / 10000;
			numberDesc = (double) Math.round(d * 10) / 10 + "万";
		} else {
			numberDesc = String.valueOf(relive);
		}
		return numberDesc;
	}

	public static void main(String[] args) {
		// double d = 12.21312312;
		// System.out.println((double) Math.round(d * 1000) / 1000);

		String a = "[\"苹果\",\"芒果\",\"猫咪\"]";
		List<String> options = JSONObject.parseArray(a, String.class);
		System.out.println(options);
		
		System.out.println((double) 1/ 2);
	}
}
