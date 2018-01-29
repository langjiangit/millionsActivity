package com.bus.chelaile.mvc;

public class QuestionParam {

	private String accountId;
	private String udid;
	private String secret;
	private int subjectId;	// 题目id
	private int activityId; // 活动id
	private String cityId;
	private String inviteCode; // 邀请码
	
	private int answer;
	
	private long timestamp; // 客户端上传的时间戳
	
	private int type; // 0 位发题、1 为发答案、2 主持人说话、3 开始计时、4 公布获奖结果
	private int msgType; // 信息类型， 0 文字，1 图片
	private String content; // 发送的内容
	
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}


	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public int getAnswer() {
		return answer;
	}

	public void setAnswer(int answer) {
		this.answer = answer;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
}
