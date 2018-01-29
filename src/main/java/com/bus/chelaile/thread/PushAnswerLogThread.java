/**
 * @author quekunkun
 *
 */
package com.bus.chelaile.thread;

import java.text.ParseException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.AnswerLog;
import com.bus.chelaile.mvc.QuestionParam;
import com.bus.chelaile.util.DateUtil;

public class PushAnswerLogThread implements Runnable {

	private QuestionParam param;

	public PushAnswerLogThread(QuestionParam param) {
		this.param = param;
	}

	private static final Logger logger = LoggerFactory.getLogger(PushAnswerLogThread.class);

	@Override
	public void run() {
		logger.info("单独收卷！ ");
		try {
			System.out.println("单独收卷 ！"+ DateUtil.getFormatTime(new Date(), "yyyy-MM-dd HH:mm:ss"));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		AnswerLog answerLog = new AnswerLog(param);
		String key = QuestionCache.getAnswerLogListKey(param.getSubjectId());
		System.out.println("key=" + key + ", 试卷内容：" + JSONObject.toJSONString(answerLog));
			
		CacheUtil.lPush(key, JSONObject.toJSONString(answerLog));
	}

}