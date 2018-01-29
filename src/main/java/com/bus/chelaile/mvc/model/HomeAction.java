package com.bus.chelaile.mvc.model;

public class HomeAction {

	private String h2; // 副标题 如 乘车路上答题 瓜分百万现金
	private String date; // 时间描述 今天 or 明天 or 具体日期
	private String time; // 答题开始时间 18：30分
	private String totalMoney; // 平分总奖金
	private Integer isLive;// 是否在直播，决定页面能否限制进入答题直播间 枚举类型 0 代表否，1代表在直播
	private Integer relive; // 登录用户返回复活卡个数
	private String inviteCode;// 登录用户返回邀请码
	private Integer canFillCode; // 是否可以填写邀请码， 0 不可， 1可

	
	public String getH2() {
		return h2;
	}

	public void setH2(String h2) {
		this.h2 = h2;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(String totalMoney) {
		this.totalMoney = totalMoney;
	}

	public Integer getIsLive() {
		return isLive;
	}

	public void setIsLive(Integer isLive) {
		this.isLive = isLive;
	}

	public Integer getRelive() {
		return relive;
	}

	public void setRelive(Integer relive) {
		this.relive = relive;
	}

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public Integer getCanFillCode() {
		return canFillCode;
	}

	public void setCanFillCode(Integer canFillCode) {
		this.canFillCode = canFillCode;
	}

}
