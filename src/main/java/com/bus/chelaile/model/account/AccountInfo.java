package com.bus.chelaile.model.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bus.chelaile.common.CacheUtil;
import com.bus.chelaile.common.QuestionCache;

public class AccountInfo {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountInfo.class);
//	private int activityId; 
	private String accountId;
	private String udid;
	private String secret;
//	private String cityId;
	
	private int cardNum; // 复活卡数目
	private String inviteCode; // 邀请码
	private int canFillCode = 1; // 是否可以填写邀请码
	
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

	public String getInviteCode() {
		return inviteCode;
	}

	public void setInviteCode(String inviteCode) {
		this.inviteCode = inviteCode;
	}

	public AccountInfo() {
		super();
	}
	
	public AccountInfo(String aid, boolean isCreateCard) {
		super();
		this.accountId = aid;
		if(isCreateCard) {
			this.inviteCode = createCode(aid);
			logger.info("生成邀请码, inviteCode={}", this.inviteCode);
		}
		// 初始化， 设置复活卡，并存入缓存，复活卡作为key，用户id作为value
		
	}

	/**
	 * 创建 邀请码、 并将对应的accountId存入redis中
	 * @param aid
	 * @return
	 */
	private String createCode(String aid) {
		
		String key = QuestionCache.getCodeCreateKey();
		int incr = (int) Math.round(Math.random() * 25);
		long result = 100000L;
		if(null != CacheUtil.getFromRedis(key)) {
			result = CacheUtil.redisIncrBy(key, incr, -1);
		} else {
			CacheUtil.setToRedis(key, -1, String.valueOf(result));
		}
		
		String code = String.valueOf(result) + Character.toString((char)('A' + incr));
		String codeKey = QuestionCache.getCodeCacheKey(code);
		CacheUtil.setToRedis(codeKey, -1, aid);
		return code;
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

	public int getCanFillCode() {
		return canFillCode;
	}

	public void setCanFillCode(int canFillCode) {
		this.canFillCode = canFillCode;
	}

	public static void main(String[] args) {

		for(int i = 0; i< 10000; i ++) {
			int a = (int) Math.round(Math.random() * 25);
			if(a > 25 || a < 0) {
				System.out.println("出现范围外的！！");
				break;
			}
		}
		
		System.out.println(Character.toString((char)('A' + 25)));
	}
}
