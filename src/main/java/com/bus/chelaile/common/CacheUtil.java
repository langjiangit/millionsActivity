package com.bus.chelaile.common;

import net.spy.memcached.internal.OperationFuture;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bus.chelaile.common.cache.ICache;
import com.bus.chelaile.common.cache.OCSCacheUtil;
import com.bus.chelaile.common.cache.PayRedisCacheImplUtil;
import com.bus.chelaile.common.cache.PushRedisCacheImplUtil;
import com.bus.chelaile.common.cache.RedisCacheImplUtil;
import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.config.PropertiesUtils;

public class CacheUtil {
	// 线上 用 ocs缓存
	private static ICache client;
	// redis缓存
	private static ICache redisClient;

	private static ICache redisPush;
	
	private static ICache redisPay;

	private static boolean isInitSuccess = false;

	protected static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

	private static final int DEFAULT_EXPIRE = 60 * 60;

	private static String cacheType = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(), "cacheType", "redis");

	private static final String PROP_OCS_HOST = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(), "ocs.host");
	private static final String PROP_OCS_PORT = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(), "ocs.port");
	private static final String PROP_OCS_USERNAME = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"ocs.username");
	private static final String PROP_OCS_PASSWORD = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"ocs.password");
	private static final String PUBMSG_CLIENT_COUNT = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"pubmsg.client_count", "pubmsg#client-count");
	private static final String PUBLISH_CHANNEL = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"pubmsg.client_key", "pubmsg#ws");

	public static void initClient() {

		if (isInitSuccess) {
			logger.info(cacheType + "已经reload成功");
			return;
		}

		System.out.println("*********** cacheType=" + cacheType);
		// TODO
		 cacheType = "redis";
		if (cacheType.equals("redis")) {
			client = new RedisCacheImplUtil();
			System.out.println("redis cache");
			logger.info("redis cache");
		} else if (cacheType.equals("ocs")) {
			client = new OCSCacheUtil(PROP_OCS_HOST, PROP_OCS_PORT, PROP_OCS_USERNAME, PROP_OCS_PASSWORD);
			logger.info("ocs cache");
		} else {
			throw new IllegalArgumentException("未找到cacheType类型");
		}
		redisClient = new RedisCacheImplUtil();
		redisPush = new PushRedisCacheImplUtil();
		redisPay = new PayRedisCacheImplUtil();
		isInitSuccess = true;
	}

	/**
	 * @param key
	 *            the Cache Key
	 * @param exp
	 *            the expiration time of the records, should not exceeds 60 * 60
	 *            * 24 * 30(30 天), 单位: 秒
	 * @param obj
	 *            缓存的对象
	 */
	public static void set(String key, int exp, Object obj) {

		client.set(key, exp, obj);

	}

	public static void setToRedis(String key, int exp, Object obj) {
		redisClient.set(key, exp, obj);
	}

	public static Object getFromRedis(String key) {
		return redisClient.get(key);
	}

	public static void incrToCache(String key, int exp) {
		redisClient.IncValue(key, exp);
	}

	public static void redisDelete(String key) {
		redisClient.delete(key);
	}

	public static Map<String, Object> getValueFromRedisByList(List<String> list) {
		return redisClient.getByList(list);
	}

	public static long redisIncrBy(String key, int number, int exp) {
		return redisClient.incrBy(key, number, exp);
	}

	// redis 有序集合 3个方法
	public static void setSortedSet(String key, long score, String value, int expire) {
		redisClient.setSortedSet(key, score, value, expire);
	}

	public static Set<String> getRangeSet(String key, long startScore, long endScore, int count) {
		return redisClient.zrangeByScore(key, startScore, endScore, count);
	}

	public static Set<String> getRevRangeSet(String key, long startScore, long endScore, int count) {
		return redisClient.zrevRangeByScore(key, endScore, startScore, count);
	}

	public static Long publish(String message) {
		return redisPush.publish(PUBLISH_CHANNEL, message);
	}
	
	/*
	 * 获取 连接数（既在线人数）
	 */
	public static Object getPubClientNumber() {
		return redisPush.get(PUBMSG_CLIENT_COUNT);
	}
	
	public static Long setHashSetValue(String key, String field, String value) {
		return redisClient.setHashSetValue(key, field, value);
	}
	
	public static String getHashSetValue(String key, String field) {
		return redisClient.getHashSetValue(key, field);
	}
	
	public static Map<String, String> getHsetAll(String key) {
		return  redisClient.getHsetAll(key);
	}

	public static void lPush(String key, String value) {
		redisClient.lpush(key, value);
	}

	public static String lpop(String key) {
		return redisClient.lpop(key);
	}

	/**
	 * 设置缓存， 默认缓存时间是1小时。
	 * 
	 * @param key
	 *            缓存的key
	 * @param obj
	 *            缓存的对象
	 */
	public static void set(String key, Object obj) {
		set(key, DEFAULT_EXPIRE, obj);
	}

	public static Object get(String key) {
		return client.get(key);
	}

	public static OperationFuture<Boolean> delete(String key) {
		return client.delete(key);
	}
	
	public static void pushMoney(String key, String value) {
		redisPay.lpush(key, value);
	}

	public static void main(String[] args) throws InterruptedException {
		initClient();
		setToRedis("a", -1, "12");
		System.out.println(getFromRedis("a"));

		System.out.println(System.currentTimeMillis());
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"11111\"}", -1);
		Thread.sleep(1);
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"22222\"}", -1);
		Thread.sleep(1);
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"33333\"}", -1);
		Thread.sleep(1);
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"44444\"}", -1);
		Thread.sleep(1);
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"55555\"}", -1);
		Thread.sleep(1);
		setSortedSet("wuli_hot", System.currentTimeMillis(), "{\"text\":\"66666\"}", -1);
		System.out.println(System.currentTimeMillis());

		Set<String> tops = getRangeSet("wuli_hot", 1513067900290l, System.currentTimeMillis(), 20); // 最新内容
		System.out.println(tops);

		Set<String> ends = getRevRangeSet("wuli_hot", 0L, 1513067900290L, 20); // 历史
		System.out.println(ends);
	}
}
