package com.bus.chelaile.service;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.Constants;
import com.bus.chelaile.dao.ActivityMapper;
import com.bus.chelaile.dao.QuestionMapper;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.thread.UpdateWathing;
import com.bus.chelaile.util.New;

public class StartService {
	@Autowired
	private ActivityMapper activityMapper;
	@Autowired
	private QuestionMapper questionMapper;
	
	protected static final Logger logger = LoggerFactory.getLogger(StartService.class);
	private static final ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
	
	public List<String> init() {
		
		StaticService.clearCache();
		
		CacheUtil.initClient();
		
		// 读取数据库内的文章和活动信息，写入缓存
		List<Answer_activity> allActivities = activityMapper.listValidActivities();
		List<Answer_subject> allQuestions = questionMapper.listValidQuestions();
		
		logger.info("查询结束, 活动有{}个，题目有{}个", allActivities.size(), allQuestions.size());
//		System.out.println("查询结束, 活动有" + allActivities.size() + "个，题目有" + allQuestions.size() + " 个");
		List<String> titles = New.arrayList();
		
		for(Answer_activity activity : allActivities) {
			StaticService.addActivity(activity);
		}
		for(Answer_subject subject : allQuestions) {
			StaticService.addSubject(subject);
		}
		
		// 启动任务，定时更新在线人数（真实人数、机器人、总人数）
		if (Constants.IS_SEDN_MACHINE) {	// 一台机器做这个工作即可
			Answer_activity activity = StaticService.getNowOrNextActivity();
			if (activity != null) {
				UpdateWathing updateW = new UpdateWathing(activity.getActivityId(), activity.getRobotMultiple());
				exec.scheduleWithFixedDelay(updateW, 10, 10, TimeUnit.SECONDS);
			}
		}
		return titles;
	}
}
