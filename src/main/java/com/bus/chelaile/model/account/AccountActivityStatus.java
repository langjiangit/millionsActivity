package com.bus.chelaile.model.account;

/**
 * 用户当前活动的答题状态
 * @author quekunkun
 *
 */
public class AccountActivityStatus {

	private boolean isRAnswer; //是否答对题
	private boolean canUsedCard; //是否使用复活卡
	private boolean isLive; //是否存活
	private int answerOrder; // 用户选择的是第几题
	
	private int order; //用户答到第几题了
	
	public AccountActivityStatus() {
		super();
	}
	
	public boolean isLive() {
		return isLive;
	}
	public void setLive(boolean isLive) {
		this.isLive = isLive;
	}
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
	public int getOrder() {
		return order;
	}
	public void setOrder(int order) {
		this.order = order;
	}

	public int getAnswerOrder() {
		return answerOrder;
	}

	public void setAnswerOrder(int answerOrder) {
		this.answerOrder = answerOrder;
	}
}
