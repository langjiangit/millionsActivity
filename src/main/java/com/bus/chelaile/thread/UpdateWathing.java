package com.bus.chelaile.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.config.PropertiesUtils;

public class UpdateWathing implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateWathing.class);
	private static final int maxOnlive = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"max.online.num", "510000"));

	private int robotMultiple;
	private int activityId;
	
	
	public UpdateWathing(int activityId2, int robotMultiple2) {
		this.robotMultiple = robotMultiple2;
		this.activityId = activityId2;
	}


	@Override
	public void run() {
		logger.info("更新在线数据");
		Object realiveO = CacheUtil.getPubClientNumber();
		int realLive = 0;
		if(realiveO != null) {
			realLive = Integer.parseInt((String)CacheUtil.getPubClientNumber());
		}
		
		logger.info("在线人数:{}", realLive);
		updateWatchLiving(realLive, activityId, getRobotMultiple());
//		CacheUtil.setToRedis(keyR, -1, realLive);
		
	}
	
	
	/**
	 * 更新在线人数
	 * 
	 * @param realLive
	 */
	private void updateWatchLiving(int realLive, int activityId, int robotMultiple) {
		
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
		int questionN = questionStatus.getQuestionN();
		
		String YJkey = QuestionCache.getYJKey(activityId, questionN);
		String YJRobotKey = QuestionCache.getYJRobotKey(activityId, questionN);
		String YJTotalKey = QuestionCache.getYJTotalKey(activityId, questionN);
		
//		String YJkeyLast = QuestionCache.getYJKey(activityId, questionN - 1);
//		String YJRobotKeyLast = QuestionCache.getYJRobotKey(activityId, questionN - 1);
//		String YJTotalKeyLast = QuestionCache.getYJTotalKey(activityId, questionN - 1);
		
		int lastRealLive = getInt(CacheUtil.getHashSetValue(YJkey, "7"));
//		int lastRobotLive = getInt(CacheUtil.getHashSetValue(YJRobotKeyLast, "7"));
		
		int maxOnline = (int) (maxOnlive + (2000 * Math.random()));
		if (realLive > maxOnline) {
			// 超出阈值
			
			CacheUtil.addHashSetValue(YJRobotKey, "7", (realLive - lastRealLive));
			CacheUtil.addHashSetValue(YJTotalKey, "7", (realLive - lastRealLive));
			CacheUtil.setHashSetValue(YJkey, "7", realLive + "");

		} else {
			if (realLive > lastRealLive) {
				// 人数增多
				int add = (int) Math.round((realLive - lastRealLive) * (0.5 + Math.random()) * robotMultiple);
				CacheUtil.addHashSetValue(YJRobotKey, "7", add);
				CacheUtil.addHashSetValue(YJTotalKey, "7", add);
				CacheUtil.addHashSetValue(YJkey, "7", realLive);
			} else {
				// 人数降低
				int sub = (int) Math.round((lastRealLive - realLive) * (0.2 + Math.random() * 0.3) * robotMultiple);
				CacheUtil.addHashSetValue(YJRobotKey, "7", sub * (-1));
				CacheUtil.addHashSetValue(YJTotalKey, "7", sub * (-1));
				CacheUtil.setHashSetValue(YJkey, "7", realLive + "");
			}
		}
	}
	


	public int getActivityId() {
		return activityId;
	}


	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}


	public int getRobotMultiple() {
		return robotMultiple;
	}


	public void setRobotMultiple(int robotMultiple) {
		this.robotMultiple = robotMultiple;
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
}
