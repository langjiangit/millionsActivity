package com.bus.chelaile.mvc;

import javax.servlet.http.HttpServletRequest;


public class AbstractController {

	public QuestionParam getActionParam(HttpServletRequest request) {
		QuestionParam param = new QuestionParam();
	
		param.setAccountId(request.getParameter("accountId"));
		param.setActivityId(getInt(request, "activityId"));
		param.setSubjectId(getInt(request, "subjectId"));
		param.setCityId(request.getParameter("cityId"));
		param.setAnswer(getInt(request, "answer"));
		param.setSecret(request.getParameter("secret"));
		param.setUdid(request.getParameter("udid"));
		
		param.setInviteCode(request.getParameter("inviteCode"));
		param.setTimestamp(getLong(request, "timestamp"));
		param.setType(getInt(request, "type"));
		param.setMsgType(getInt(request, "msgType"));
		param.setContent(request.getParameter("content"));
		param.setNickName(request.getParameter("nickName"));
		
		return param;
	}
	
	protected static int getInt(HttpServletRequest request, String paramName) {
		String value = request.getParameter(paramName);
		if (value == null || value.length() == 0) {
			return -1;
		} else {
			try {
				return Integer.parseInt(value);
			} catch (Exception e) {
				return -1;
			}
		}
	}
	
	protected static long getLong(HttpServletRequest request, String paramName) {
		String value = request.getParameter(paramName);
		if (value == null || value.length() == 0) {
			return -1L;
		} else {
			try {
				return Long.parseLong(value);
			} catch (Exception e) {
				return -1L;
			}
		}
	}
}
