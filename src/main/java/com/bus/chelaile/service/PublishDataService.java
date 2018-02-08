package com.bus.chelaile.service;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
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
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.model.account.MoneyModel;
import com.bus.chelaile.model.pubmsg.PubAnswer;
import com.bus.chelaile.model.pubmsg.PubCustomInfo;
import com.bus.chelaile.model.pubmsg.PubOptions;
import com.bus.chelaile.model.pubmsg.UserInfo;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.util.HttpUtils;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;

public class PublishDataService {

	@Autowired
	private ServiceManager serviceManager;
	protected static final Logger logger = LoggerFactory.getLogger(PublishDataService.class);
	private static final String ACCOUNT_URL = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"account.list.info", "http://api.chelaile.net.cn/wow/user!accountList.action?accountList=");
	private static final String GET_ROBOT_ACCOUNT = "http://web.chelaile.net.cn/outman/social/getInvalidPeople";

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

		Map<String, Object> sendAnswer = New.hashMap();
		sendAnswer.put("a", pubAnswer);
		sendAnswer.put("s", System.currentTimeMillis());

		// 发布答案, 更新状态
		CacheUtil.publish(JSONObject.toJSONString(sendAnswer));
		questionStatus.setQuestionS(3);
		QuestionCache.updateQuestionStatus(question.getActivityId(), questionStatus);
	}

	/**
	 * 推送开场倒计时
	 */
	public String sendBeginMsg() {
		Map<String, Object> beginMsg = New.hashMap();
		beginMsg.put("start", 5);
		beginMsg.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(beginMsg));

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
		Map<String, Object> emceeMsg = New.hashMap();
		JSONObject msg = new JSONObject();
		msg.put("t", msgType);
		msg.put("c", content);

		emceeMsg.put("m", msg);
		emceeMsg.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(emceeMsg));

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
		Map<String, Object> emceeMsg = New.hashMap();
		JSONObject msg = new JSONObject();
		msg.put("n", nickName);
		msg.put("c", content);

		emceeMsg.put("c", msg);
		emceeMsg.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(emceeMsg));

		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 宣布活动无效！！！
	 * 
	 * @return
	 */
	public String sendFinishMsg() {
		Map<String, Object> finishMsg = New.hashMap();
		finishMsg.put("finished", 1);
		finishMsg.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(finishMsg));

		return serviceManager.getClienSucMap("Send finish msg success", Constants.STATUS_REQUEST_SUCCESS);
	}
	
	public String sendRestartMsg() {
		Map<String, Object> finishMsg = New.hashMap();
		finishMsg.put("replay", 1);
		finishMsg.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(finishMsg));

		return serviceManager.getClienSucMap("Send finish msg success", Constants.STATUS_REQUEST_SUCCESS);
	}

	/**
	 * 推送 通关消息
	 */
	public String sendResult(QuestionParam param) {

		PubCustomInfo puaCustomInfo = new PubCustomInfo();

		Answer_activity activity = StaticService.getNowOrNextActivity();
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activity.getActivityId());
		String livePeopleKey = QuestionCache.getLiveKey(activity.getActivityId(), questionStatus.getQuestionN());

		Set<String> allLives = CacheUtil.getSet(livePeopleKey);
		StringBuilder accountStr = new StringBuilder();

		logger.info("活动结束：{}，发送结果，真实过关用户数为：{}, livePeopleKey={}", activity.getActivityId(), allLives.size(),
				livePeopleKey);

		int size = 0;
		String url = ACCOUNT_URL;
		for (String accountId : allLives) {
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
		} catch (Exception e) {
			logger.error("获取用户信息出错, url={}", url, e);
		}

		// 这里计算方式有待商榷，目前定为：当前题目的答对人数 + 使用复活卡的人数
		AnswerData answerData = QuestionCache.getAnsweData(activity.getActivityId(), questionStatus.getQuestionN());
		int number = answerData.getRightOptionNum().getTotal().get() + answerData.getReLive().getTotal().get();

		if (puaCustomInfo.getuL().size() < 16) {
			addRobotAccount(puaCustomInfo);
		}
		puaCustomInfo.setU(number);
		DecimalFormat df = new DecimalFormat("0.00");// 格式化小数
		String s = df.format((double) activity.getTotalBonus() / number);// 返回的是String类型
		puaCustomInfo.setM(s);

		// 推送中奖结果
		Map<String, Object> pubResult = New.hashMap();
		pubResult.put("over", puaCustomInfo);
		pubResult.put("s", System.currentTimeMillis());
		CacheUtil.publish(JSONObject.toJSONString(pubResult));

		// 添加金额
		for (String accountId : allLives) {
			logger.info("添加奖金， accountId={}, money={}", accountId, s);
			MoneyModel moneyModel = new MoneyModel(accountId, 1,
					String.valueOf(Math.round(Double.parseDouble(s) * 100)));
			logger.info("添加流水, moneyModel={}", JSONObject.toJSONString(moneyModel));
			CacheUtil.pushMoney("drawMoneyQueue", JSONObject.toJSONString(moneyModel));
		}

		return serviceManager.getClienSucMap("Send success", Constants.STATUS_REQUEST_SUCCESS);
	}

	private void addRobotAccount(PubCustomInfo puaCustomInfo) {
		String url = GET_ROBOT_ACCOUNT;
		int length = 16 - puaCustomInfo.getuL().size();
		logger.info("实际中奖人数不够，凑机器人，机器人数量为{}", length);
		int i = 0;
		while(i < length) {
			try {
				String response = HttpUtils.get(url, "utf-8");
				JSONObject responseJ = JSONObject.parseObject(response);
				if (responseJ != null && StringUtils.isNoneBlank(responseJ.getString("nickname"))
						&& StringUtils.isNoneBlank(responseJ.getString("photoUrl"))) {
					UserInfo user = new UserInfo();
					user.setName(responseJ.getString("nickname"));
					user.setImg(responseJ.getString("photoUrl"));
					puaCustomInfo.getuL().add(user);
					i ++ ;
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

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
		 double d = 12.21312312;
		 System.out.println((double) Math.round(d * 1000) / 1000);

		 String a = "[\"苹果\",\"芒果\",\"猫咪\"]";
		 List<String> options = JSONObject.parseArray(a, String.class);
		 System.out.println(options);
		
		 System.out.println((double) 1 / 2);
		
		 System.out.println((double) Math.round(((double) 1999) * 100) / 100);
		
		 float num = (float) 2 / 1;
		 DecimalFormat df = new DecimalFormat("0.00");// 格式化小数
		 String s = df.format(num);// 返回的是String类型
		 System.out.println(s);
		
		System.out.println("##### 金钱  test");
		CacheUtil.initClient(); // 记得手动设置redis 哟
		MoneyModel moneyModel = new MoneyModel("4941935", 1,
				String.valueOf(Math.round(Double.parseDouble("8888.88") * 100)));
		System.out.println(JSONObject.toJSONString(moneyModel));
		CacheUtil.pushMoney("drawMoneyQueue", JSONObject.toJSONString(moneyModel));

//		Map<String, Object> beginMsg = New.hashMap();
//		beginMsg.put("start", 5);
//		beginMsg.put("s", System.currentTimeMillis());
//		System.out.println(JSONObject.toJSONString(beginMsg));
		
		
		Map<String, Object> finishMsg = New.hashMap();
		finishMsg.put("pause", 1);
		finishMsg.put("s", System.currentTimeMillis());
		System.out.println((JSONObject.toJSONString(finishMsg)));
	}

}
