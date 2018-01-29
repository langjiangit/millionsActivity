package com.bus.chelaile.model.pubmsg;

public class PubOptions {
	private String d; // 题描述
	private String n; // 回答人数 '1.6万 or 2341'
	private double p; // 答案占比， 0.09
	
	public PubOptions() {
		super();
	}
	public PubOptions(String d, String n, double p) {
		super();
		this.d = d;
		this.n = n;
		this.p = p;
	}
	public String getD() {
		return d;
	}
	public void setD(String d) {
		this.d = d;
	}
	public String getN() {
		return n;
	}
	public void setN(String n) {
		this.n = n;
	}
	public double getP() {
		return p;
	}
	public void setP(double p) {
		this.p = p;
	}
}
