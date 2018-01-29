package com.bus.chelaile.model.client;

import java.io.Serializable;

import com.alibaba.fastjson.JSONObject;

/**
 * 给客户端返回的最外层对象
 * @author zzz
 *
 */
public class ClientDto implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JsonStr jsonr;
	


	public JsonStr getJsonr() {
		return jsonr;
	}

	public void setJsonr(JsonStr jsonr) {
		this.jsonr = jsonr;
	}

	public void setSuccessObject(Object data, String status){
		this.jsonr = new JsonStr();
		jsonr.setStatus(status);
		jsonr.setData(data);
	}
	
	public void setErrorObject(String errmsg, String status){
		this.jsonr = new JsonStr();
		jsonr.setData(new JSONObject());
		jsonr.setErrmsg(errmsg);
		jsonr.setStatus(status);
	}
}


