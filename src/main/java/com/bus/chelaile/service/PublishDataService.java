package com.bus.chelaile.service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.AnswerData;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.model.pubmsg.PubAnswer;
import com.bus.chelaile.model.pubmsg.PubCustomInfo;
import com.bus.chelaile.model.pubmsg.PubOptions;
import com.bus.chelaile.model.pubmsg.UserInfo;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.util.HttpUtils;
import com.bus.chelaile.util.New;

public class PublishDataService {

	@Autowired
	private ServiceManager serviceManager;
	protected static final Logger logger = LoggerFactory.getLogger(PublishDataService.class);
	private static final String ACCOUNT_URL = "http://dev.chelaile.net.cn/wow/user!accountList.action?accountList=";

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

		Answer_activity activity = StaticService.getNowOrNextActivity();
		// 此处： 关于多个节点的情况 考虑欠周到
		activity.setIsonLive(1);
		activity.setStatus(1);

		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 推送主持人说的话
	 * 
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

		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 推送 聊天室 消息
	 * 
	 * @param msgType
	 * @param content
	 * @return
	 */
	public String sendChatMsg(String nickName, String content) {
		Map<String, JSONObject> emceeMsg = New.hashMap();
		JSONObject msg = new JSONObject();
		msg.put("n", nickName);
		msg.put("c", content);

		emceeMsg.put("c", msg);
		CacheUtil.publish("ws", JSONObject.toJSONString(emceeMsg));

		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 推送 通关消息
	 */
	public String sendResult(QuestionParam param) {

		PubCustomInfo puaCustomInfo = new PubCustomInfo();

		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(param.getActivityId());
		Answer_activity activity = StaticService.getNowOrNextActivity();
		String livePeopleKey = QuestionCache.getLivePeoPleKey(activity.getActivityId(), questionStatus.getQuestionN());

		Map<String, String> allAnswers = CacheUtil.getHsetAll(livePeopleKey);
		StringBuilder accountStr = new StringBuilder();

		logger.info("发送结果，过关用户数为：{}", allAnswers.size());
		
		int size = 0;
		String url = ACCOUNT_URL;
		for (String accountId : allAnswers.keySet()) {
			accountStr.append(accountId).append(",");
			logger.info("accountId={}, accountStr={}", accountId, accountStr.toString());
			if (++size >= 16) {
				break;
			}
		}
		url += accountStr.toString();
		logger.info("url={}", url);
		try {
			String response = HttpUtils.get(url, "utf-8");
			String responseJStr = response.substring(6, response.length() - 6);
			JSONObject responseJ = JSONObject.parseObject(responseJStr);
			JSONArray accountsJ = responseJ.getJSONObject("jsonr").getJSONObject("data").getJSONArray("accountInfos");
			for (int i = 0; i < accountsJ.size(); i++) {
				UserInfo user = new UserInfo();
				user.setImg(accountsJ.getJSONObject(i).getString("photoUrl"));
				user.setName(accountsJ.getJSONObject(i).getString("nickName"));
				puaCustomInfo.getuL().add(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		AnswerData answerData = StaticService.getAnswerData(activity.getActivityId(), questionStatus.getQuestionN());
		int number = answerData.getAnswerNum().getTotal().get();

		puaCustomInfo.setU(number);
		DecimalFormat df = new DecimalFormat("0.00");// 格式化小数
		String s = df.format((double) activity.getTotalBonus() / number);// 返回的是String类型
		puaCustomInfo.setM(s);

		Map<String, PubCustomInfo> pubResult = New.hashMap();
		pubResult.put("over", puaCustomInfo);
		CacheUtil.publish("ws", JSONObject.toJSONString(pubResult));
		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
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

		System.out.println((double) 1 / 2);

		System.out.println((double) Math.round(((double) 1999) * 100) / 100);

		float num = (float) 2 / 1;
		DecimalFormat df = new DecimalFormat("0.00");// 格式化小数
		String s = df.format(num);// 返回的是String类型
		System.out.println(s);
	}
}
