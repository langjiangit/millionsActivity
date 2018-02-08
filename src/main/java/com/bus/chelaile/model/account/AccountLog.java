package com.bus.chelaile.model.account;

import com.bus.chelaile.model.AnswerLog;

public class AccountLog {
	
	private AnswerLog answerLog;
	private int cardNum;
	private boolean isLive; //是否存活
	private boolean isRAnswer; //是否答对题
	private boolean canUsedCard; //是否使用复活卡
	
	public boolean isRAnswer() {
		return isRAnswer;
	}

	public void setRAnswer(boolean isRAnswer) {
		this.isRAnswer = isRAnswer;
	}

	public boolean isCanUsedCard() {
		return canUsedCard;
	}

	public void setCanUsedCard(boolean canUsedCard) {
		this.canUsedCard = canUsedCard;
	}

	public boolean isLive() {
		return isLive;
	}

	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}

	public AccountLog() {
		super();
	}
	
	public AnswerLog getAnswerLog() {
		return answerLog;
	}
	public void setAnswerLog(AnswerLog answerLog) {
		this.answerLog = answerLog;
	}
	public int getCardNum() {
		return cardNum;
	}
	public void setCardNum(int cardNum) {
		this.cardNum = cardNum;
	}  
}
