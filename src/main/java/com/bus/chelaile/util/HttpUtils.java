/**
 * Copyright ©2015 元光科技 All Rights Reserved
 */
package com.bus.chelaile.util;

import java.io.*;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.config.PropertiesUtils;

/**
 * @author zzj 2015年4月25日
 * @mail zhijian.zhang@chelaile.net.cn
 */
public class HttpUtils {

	public static final PoolingHttpClientConnectionManager CM = new PoolingHttpClientConnectionManager();
	
	private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	
	private static final int connectTimeout = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"connectTimeout", "30"));;
	private static final int connectionRequestTimeout = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"connectionRequestTimeout", "30"));;
	private static final int socketTimeout = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(),
			"socketTimeout", "30"));;

	/**
	 * 初始 连接池
	 */
	static {
		CM.setMaxTotal(20000);
		CM.setDefaultMaxPerRoute(Integer.parseInt(PropertiesUtils.getValue(PropertiesName.PUBLIC.getValue(), "default.max.per.route", "500")));
		// 校验失效的链接
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				CM.closeExpiredConnections();
				CM.closeIdleConnections(30, TimeUnit.SECONDS);
			}
		}, 0, 10 * 1000);
	}

	public static final RequestConfig REQUEST_CONFIG = RequestConfig.custom()
	// 建立链接超时
			.setConnectTimeout(1000 * connectTimeout)
			// 从conn mg 中获取链接超时
			.setConnectionRequestTimeout(connectionRequestTimeout * 1000)
			// 请求数据超时
			.setSocketTimeout(1000 * socketTimeout).build();

	public static final CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
			.setConnectionManager(CM)
			.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
				@Override
				public long getKeepAliveDuration(HttpResponse response,
						HttpContext context) {
					// Honor 'keep-alive' header
					HeaderElementIterator it = new BasicHeaderElementIterator(
							response.headerIterator(HTTP.CONN_KEEP_ALIVE));
					while (it.hasNext()) {
						HeaderElement he = it.nextElement();
						String param = he.getName();
						String value = he.getValue();
						System.out.printf("请求参数：%s，值：%s \n", param, value);
						if (value != null && param.equalsIgnoreCase("timeout")) {
							try {
								return Long.parseLong(value) * 1000;
							} catch (NumberFormatException ignore) {
							}
						}
					}
					return 20 * 1000;
				}
			}).setDefaultRequestConfig(REQUEST_CONFIG).build();

	public static String get(String url, List<NameValuePair> params,
			String encode) throws ParseException, UnsupportedEncodingException,
			IOException {
		String res = null;
		if (params != null && params.size() > 0) {
			url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params));
		}
		logger.info("url={}", url);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			response = HTTP_CLIENT.execute(get, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	
	/*
	 * NameValuePair形式的参数
	 */
	public static String post(String url, List<NameValuePair> params,
			String encode) throws ParseException, UnsupportedEncodingException,
			IOException {
		String res = null;
		if (params != null && params.size() > 0) {
			url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params));
		}
		HttpPost  post = new HttpPost(url);
		CloseableHttpResponse response = null;
		try {
			response = HTTP_CLIENT.execute(post);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	

	public static String get(String url, String encode)
			throws ClientProtocolException, IOException {
		String res = null;
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
//			get.setHeader("X-Forwarded-For", "111.111.111.1");
			response = HTTP_CLIENT.execute(get, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	
	public static String getAndSetIp(String url, String encode, String x_forwarded_for)
			throws ClientProtocolException, IOException {
		String res = null;
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			get.setHeader("X-Forwarded-For", x_forwarded_for);
			response = HTTP_CLIENT.execute(get, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	
	public static String getUriAndSetIp(String url, List<NameValuePair> params,
			String encode, String x_forwarded_for) throws ParseException, UnsupportedEncodingException,
			IOException, URISyntaxException {
		String res = null;
		if (params != null && params.size() > 0) {
			url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params));
		}
		System.out.println(url);
//		URL url1 = new URL(url);
//		URI uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			get.setHeader("X-Forwarded-For", x_forwarded_for);
			response = HTTP_CLIENT.execute(get, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	
	
	public static String getUri(String url, List<NameValuePair> params,
			String encode) throws ParseException, UnsupportedEncodingException,
			IOException, URISyntaxException {
		String res = null;
		if (params != null && params.size() > 0) {
			url += "?" + EntityUtils.toString(new UrlEncodedFormEntity(params));
		}
		System.out.println(url);
//		URL url1 = new URL(url);
//		URI uri = new URI(url1.getProtocol(), url1.getHost(), url1.getPath(), url1.getQuery(), null);
		HttpGet get = new HttpGet(url);
		CloseableHttpResponse response = null;
		try {
			response = HTTP_CLIENT.execute(get, HttpClientContext.create());
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				res = EntityUtils.toString(entity, encode);
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return res;
	}
	

	public static void main(String[] args) throws ParseException,
			UnsupportedEncodingException, IOException {

//		String baiduUri = "http://api.map.baidu.com/geocoder/v2/?ak=a973f8b492f8c72de4fd099a318c17b56&location=39.136727,117.249471&output=json";
//		System.out.println(get(baiduUri, null, "utf-8"));
//		// 高德的几个key
//		// da8dd537807d127e3d41c74cbf185836
//		// eaa6713cc90f6a8f556c3f6ff1ca0542
//		// 8325164e247e15eea68b59e89200988b
//		System.out
//				.println(get(
//						"http://restapi.amap.com/v3/geocode/regeo?location=117.249471,39.136727&key=ada8dd537807d127e3d41c74cbf185836",
//						null, "utf-8"));
		
//		System.out.println(post("http://watsons.weixinyiwindow.com/activity/qrcode4in1/index.html?mch=kugou&unique_id=a354fd5bc9&sign=7c20fb89a7fee802dc8ff0edb4d72e14", null, "utf-8"));

		
		
		
		

	}
}
