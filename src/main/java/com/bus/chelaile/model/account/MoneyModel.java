package com.bus.chelaile.model.account;

import java.text.ParseException;
import java.util.Date;

import com.bus.chelaile.util.DateUtil;

public class MoneyModel {
	private String userId;
	private int channel;
	private String money;
	private String time;
	private String serialNumber;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getSerialNumber() {
		return serialNumber;
	}
	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
	
	
	public MoneyModel() {
		super();
	}
	
	public MoneyModel(String userId, int channel, String money) {
		super();
		
		this.userId = userId;
		this.channel = channel;
		this.money = money;
		try {
			this.time = DateUtil.getFormatTime(new Date(), "yyyy-MM-dd HH:mm:ss");
			this.serialNumber = "bus_" + System.currentTimeMillis() + userId;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	

}
