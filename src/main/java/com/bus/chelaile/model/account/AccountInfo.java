package com.bus.chelaile.model.account;

public class AccountInfo {

//	private int activityId; 
	private String accountId;
	private String udid;
	private String secret;
	private String cityId;
	
	private int cardNum; // 复活卡数目
	private String inviteCode; // 邀请码
	
	private boolean canInvite; // 是否可以填写邀请码

	public AccountInfo() {
		super();
	}

	public int getCardNum() {
		return cardNum;
	}

	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}

	public void reduceOne() {
		this.cardNum --;
		
	}

	
	
}
