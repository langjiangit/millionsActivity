package com.bus.chelaile.model.pubmsg;

import java.util.List;

/**
 * 下发的答案
 * @author quekunkun
 *
 */
public class PubAnswer {

	private int k;   // 题目唯一key
	private int n;	 // 题目序号，当前第几题
	private String t;// 问题
	private int a;   // 正确的答案，
	private String r; //复活人数  '1.6万 or 2341'，
	private List<PubOptions> c;
	
	public int getK() {
		return k;
	}
	public void setK(int k) {
		this.k = k;
	}
	public int getN() {
		return n;
	}
	public void setN(int n) {
		this.n = n;
	}
	public String getT() {
		return t;
	}
	public void setT(String t) {
		this.t = t;
	}
	public int getA() {
		return a;
	}
	public void setA(int a) {
		this.a = a;
	}
	public String getR() {
		return r;
	}
	public void setR(String r) {
		this.r = r;
	}
	public List<PubOptions> getC() {
		return c;
	}
	public void setC(List<PubOptions> c) {
		this.c = c;
	}
}
