package com.bus.chelaile.model;

public class NumErea {

	private int min;
	private int max;
	private int out;
	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
	public int getMax() {
		return max;
	}
	public void setMax(int max) {
		this.max = max;
	}
	
	public NumErea() {
		super();
	}
	public NumErea(String min1, String max1, String out1) {
		super();
		this.min = Integer.parseInt(min1);
		this.max = Integer.parseInt(max1);
		this.out = Integer.parseInt(out1);
	}
	public int getOut() {
		return out;
	}
	public void setOut(int out) {
		this.out = out;
	}
}
