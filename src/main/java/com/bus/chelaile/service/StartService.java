package com.bus.chelaile.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.dao.ActivityMapper;
import com.bus.chelaile.dao.QuestionMapper;
import com.bus.chelaile.model.Answer_activity;
import com.bus.chelaile.model.Answer_subject;
import com.bus.chelaile.util.New;

public class StartService {
	@Autowired
	private ActivityMapper activityMapper;
	@Autowired
	private QuestionMapper questionMapper;
	
	protected static final Logger logger = LoggerFactory.getLogger(StartService.class);
	
	public List<String> init() {
		
		StaticService.clearCache();
		
		CacheUtil.initClient();
		
		// 读取数据库内的文章和活动信息，写入缓存
		List<Answer_activity> allActivities = activityMapper.listValidActivities();
		List<Answer_subject> allQuestions = questionMapper.listValidQuestions();
		
		System.out.println("查询结束, 活动有" + allActivities.size() + "个，题目有" + allQuestions.size() + " 个");
		List<String> titles = New.arrayList();
		
		for(Answer_activity activity : allActivities) {
			StaticService.addActivity(activity);
		}
		for(Answer_subject subject : allQuestions) {
			StaticService.addSubject(subject);
		}
		
		return titles;
	}
}
