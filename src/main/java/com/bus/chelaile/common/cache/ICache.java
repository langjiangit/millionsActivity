package com.bus.chelaile.common.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.spy.memcached.internal.OperationFuture;

public interface ICache {
	 public void set(String key, int exp, Object obj);
	 
	 public Object get(String key);
	
	 public OperationFuture<Boolean> delete(String key);
	 
	 public Map<String, Object> getByList(List<String> list);
	 
	 
	 public long IncValue(String key, int exp);
	 
	 //
	 public long incrBy(String key, int incNumber, int exp);

	 // 从redis直接获取set
	 public Set<String> getSet(String key);
	 
	 // 发布消息到redis频道
	 public Long publish(String channel, String message);
	 
	 public void lpush(String key, String value);
	 
	 public String lpop(String key);
	 
	 
	 // redis 有序集合
	 public void setSortedSet(String key, double score,String value, int expire);
	 public Set<String> zrangeByScore(String key, double startScore, double endScore, int count);
	 public Set<String> zrevRangeByScore(String key, double endScore, double startScore, int count);

	public Long setHashSetValue(String key, String field, String value);

	public String getHashSetValue(String key, String field);

	public Map<String, String> getHsetAll(String key);

	public void addHashSetValue(String yJkey, String field, int j);
	
	void sadd(String key, String members);

	Set<String> sdiff(String s1, String s2);

	public Set<String> getHKeys(String key);
}
