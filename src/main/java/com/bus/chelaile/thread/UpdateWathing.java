package com.bus.chelaile.thread;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;
import com.bus.chelaile.model.ActivityStatus;
import com.bus.chelaile.model.NumErea;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;

public class UpdateWathing implements Runnable{
	
	private static final Logger logger = LoggerFactory.getLogger(UpdateWathing.class);
	private static final int MAM_ONLINE = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"max.online.num", "510000"));
	private static final String onlineConfig = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"config.num", "510000");
	private static final List<NumErea> NUM_EREAS = New.arrayList();

	private int robotMultiple;
	private int activityId;
	
	static {
		String[] cons = onlineConfig.split(",");
		for(String c : cons) {
			String min = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(), "real." + c + ".min");
			String max = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(), "real." + c + ".max");
			String out = PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(), "out." + c);
			NumErea n = new NumErea(min, max, out);
			NUM_EREAS.add(n);
		}
	}
	
	public UpdateWathing(int activityId2, int robotMultiple2) {
		this.robotMultiple = robotMultiple2;
		this.activityId = activityId2;
	}


	@Override
	public void run() {
		Object realiveO = CacheUtil.getPubClientNumber();
		int realLive = 0;
		if(realiveO != null) {
			realLive = Integer.parseInt((String)CacheUtil.getPubClientNumber());
		}
		
		if(realLive == 0) {
			return;
		}
		logger.info("在线人数:{}", realLive);
		updateWatchLiving(realLive, activityId, getRobotMultiple());
	}
	
	
	/**
	 * 更新在线人数
	 * 其实这个数跟 qN无关，但是挂在YJKey下面，被迫需要继承上一个qN的状态
	 * @param realLive
	 */
	private void updateWatchLiving(int realLive, int activityId, int robotMultiple) {
		ActivityStatus questionStatus = QuestionCache.getQuestionStatus(activityId);
		int questionN = questionStatus.getQuestionN();
		
		String YJkey = QuestionCache.getYJKey(activityId, questionN);
		String YJRobotKey = QuestionCache.getYJRobotKey(activityId, questionN);
		String YJTotalKey = QuestionCache.getYJTotalKey(activityId, questionN);
		
		String YJkeyLast = QuestionCache.getYJKey(activityId, questionN - 1);
		String YJRobotKeyLast = QuestionCache.getYJRobotKey(activityId, questionN - 1);
		String YJTotalKeyLast = QuestionCache.getYJTotalKey(activityId, questionN - 1);
		
		 // 第一次查询为0，继承上一次的数据
		int beforeRealLive = getInt(CacheUtil.getHashSetValue(YJkey, "7"));
		if(beforeRealLive == 0) {
			if(questionN >= 0) {
				CacheUtil.setHashSetValue(YJkey, "7", CacheUtil.getHashSetValue(YJkeyLast, "7"));
				CacheUtil.setHashSetValue(YJRobotKey, "7", CacheUtil.getHashSetValue(YJRobotKeyLast, "7"));
				CacheUtil.setHashSetValue(YJTotalKey, "7", CacheUtil.getHashSetValue(YJTotalKeyLast, "7"));
				return;
			} else {
				CacheUtil.setHashSetValue(YJkey, "7", realLive + "");
				CacheUtil.setHashSetValue(YJRobotKey, "7", realLive * robotMultiple + "");
				CacheUtil.setHashSetValue(YJTotalKey, "7", realLive * (robotMultiple + 1) + "");
				return;
			}
		}
		
//		int maxOnlineO = (int) (MAM_ONLINE + (2000 * Math.random()));
		int maxOnlineO = getOutNum(realLive);
		logger.info("maxOnlineO={}", maxOnlineO);
		if (realLive > maxOnlineO) {
			// 超出阈值
			CacheUtil.addHashSetValue(YJRobotKey, "7", (realLive - beforeRealLive));
			CacheUtil.addHashSetValue(YJTotalKey, "7", (realLive - beforeRealLive));
			CacheUtil.setHashSetValue(YJkey, "7", realLive + "");

		} else {
			if (realLive > beforeRealLive) {
				// 人数增多
				int add = (int) Math.round((realLive - beforeRealLive) * (0.5 + Math.random()) * robotMultiple);
				CacheUtil.addHashSetValue(YJRobotKey, "7", add);
				CacheUtil.addHashSetValue(YJTotalKey, "7", add);
				CacheUtil.addHashSetValue(YJkey, "7", realLive);
			} else {
				// 人数降低
				int sub = (int) Math.round((beforeRealLive - realLive) * (0.2 + Math.random() * 0.3) * robotMultiple);
				CacheUtil.addHashSetValue(YJRobotKey, "7", sub * (-1));
				CacheUtil.addHashSetValue(YJTotalKey, "7", sub * (-1));
				CacheUtil.setHashSetValue(YJkey, "7", realLive + "");
			}
		}
	}
	
	
	/**
	 * 根据实际在线人数，动态获取最高人数上限
	 * @param realLive
	 * @return
	 */
	private int getOutNum(int realLive) {
		for(NumErea num : NUM_EREAS) {
			if(realLive <= num.getMax() && realLive >= num.getMin()) {
				return num.getOut();
			}
		}
		return 0;
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
