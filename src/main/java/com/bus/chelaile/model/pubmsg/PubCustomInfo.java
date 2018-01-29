package com.bus.chelaile.model.pubmsg;

import java.util.List;
import com.bus.chelaile.util.New;

public class PubCustomInfo {

	private int u;
	private String m;
	private List<UserInfo> uL = New.arrayList();
	
	public int getU() {
		return u;
	}
	public void setU(int u) {
		this.u = u;
	}
	public String getM() {
		return m;
	}
	public void setM(String m) {
		this.m = m;
	}
	public List<UserInfo> getuL() {
		return uL;
	}
	public void setuL(List<UserInfo> uL) {
		this.uL = uL;
	}
}
