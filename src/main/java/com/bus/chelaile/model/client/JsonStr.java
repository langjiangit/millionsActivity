package com.bus.chelaile.model.client;

import java.io.Serializable;

public class JsonStr implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Object data;
	private String errmsg = "";
	private String status = "00";
	private String sversion = "";

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getErrmsg() {
		return errmsg;
	}

	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSversion() {
		return sversion;
	}

	public void setSversion(String sversion) {
		this.sversion = sversion;
	}

}
