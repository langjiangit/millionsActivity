package com.bus.chelaile.model;

import java.util.concurrent.atomic.AtomicInteger;

public class NumbersModel {

	private AtomicInteger total = new AtomicInteger();
	private AtomicInteger realNum = new AtomicInteger();
	private AtomicInteger robotNum = new AtomicInteger();
	
	public AtomicInteger getTotal() {
		return total;
	}
	public void setTotal(AtomicInteger total) {
		this.total = total;
	}
	public AtomicInteger getRealNum() {
		return realNum;
	}
	public void setRealNum(AtomicInteger realNum) {
		this.realNum = realNum;
	}
	public AtomicInteger getRobotNum() {
		return robotNum;
	}
	public void setRobotNum(AtomicInteger robotNum) {
		this.robotNum = robotNum;
	}
	
	public static void main(String[] args) {
		NumbersModel n = new NumbersModel();
		System.out.println(n.getRealNum());
		n.getRealNum().addAndGet(1);
		n.getRobotNum().addAndGet(12);
		System.out.println(n.getRealNum());
		System.out.println(n.getRobotNum());
	}
}
