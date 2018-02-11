package com.bus.chelaile.common.cache;

import net.spy.memcached.internal.OperationFuture;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import com.bus.chelaile.model.PropertiesName;
import com.bus.chelaile.util.New;
import com.bus.chelaile.util.config.PropertiesUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PushRedisCacheImplUtil implements ICache {
	private static Logger log = LoggerFactory.getLogger(PushRedisCacheImplUtil.class);

	protected static final Logger logger = LoggerFactory.getLogger(PushRedisCacheImplUtil.class);

	private static String PUSH_REDIS_HOST = PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"push.redis.host", "10.174.97.52");
	private static int PUSH_REDIS_PORT = Integer.parseInt(PropertiesUtils.getValue(PropertiesName.CACHE.getValue(),
			"push.redis.port", "6379"));
	
	private static JedisPool pool = null;

	static {
		initPool();
	}

	private static void initPool() {
		JedisPoolConfig config = new JedisPoolConfig();
		 String host = PUSH_REDIS_HOST;
		 int port = PUSH_REDIS_PORT;
		config.setMaxTotal(400);
		// config.setMaxActive(400);
		config.setMaxIdle(200);
		config.setMinIdle(20);

		// config.setMaxWait(2000000);
		// config.setMaxWaitMillis();
//		config.setTestWhileIdle(true);
//		config.setTestOnBorrow(true);
//		config.setTestOnReturn(true);

		System.out.println("***** redis初始化， redis info , host=" + host + ", port=" + port);

		pool = new JedisPool(config, host, port);

		log.info("RedisCacheImplUtil init success,ip={},host={}", host, port);
	}

	private static JedisPool getPool() {
		if (pool == null) {
			initPool();
		}
		return pool;
	}

	public void set(String key, String value, int expire) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.set(key, value);
			if (expire != -1)
				conn.expire(key, expire);
			log.debug("Redis-Set: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s value=%s, error message: " + e.getMessage(), key,
					value));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	public void setList(String key, String value, int expire) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.lpush(key, value);
			if (expire != -1)
				conn.expire(key, expire);
			log.debug("Redis-Set: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s value=%s, error message: " + e.getMessage(), key,
					value));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	public void setSet(String key, String value, int expire) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.sadd(key, value);
			if (expire != -1)
				conn.expire(key, expire);
			log.debug("Redis-Set: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s value=%s, error message: " + e.getMessage(), key,
					value));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	/**
	 * 缓存 有序集合
	 * 
	 * @param key
	 * @param score
	 * @param value
	 * @param expire
	 */
	public void setSortedSet(String key, double score, String value, int expire) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.zadd(key, score, value);
			if (expire != -1)
				conn.expire(key, expire);
			log.debug("Redis-SortedSet: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s value=%s, error message: " + e.getMessage(), key,
					value));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	/**
	 * 按照score，从小到大获取值。 起始score为startScore，从0开始获取count个值
	 * 
	 * @return
	 */
	public Set<String> zrangeByScore(String key, double startScore, double endScore, int count) {
		JedisPool pool = null;
		Jedis conn = null;
		Set<String> value = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			value = conn.zrangeByScore(key, startScore, endScore, 0, count);
			log.debug("Redis-SortedGet: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format(
					"Error occur in Redis.set, key=%s startScore=%s, endScore=%s, error message: " + e.getMessage(),
					key, startScore, endScore));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return value;
	}

	/**
	 * 按照score，从大到小获取值。 起始score为endScore，从0开始获取count个值
	 * 
	 * @return
	 */
	public Set<String> zrevRangeByScore(String key, double endScore, double startScore, int count) {
		JedisPool pool = null;
		Jedis conn = null;
		Set<String> value = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			value = conn.zrevrangeByScore(key, endScore, startScore, 0, count);
			log.debug("Redis-SortedGet: Key=" + key + ",Value=" + value);
		} catch (Exception e) {
			log.error(String.format(
					"Error occur in Redis.set, key=%s startScore=%s, endScore=%s, error message: " + e.getMessage(),
					key, startScore, endScore));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return value;
	}

	public void clearDB() {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.flushDB();
			log.debug("Redis-clearDB");
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.flushDB"));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	public Set allKeys(String patternstr) {
		JedisPool pool = null;
		Jedis conn = null;
		Set result = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			result = conn.keys(patternstr);
			log.debug("Redis-Keys: pattern=" + patternstr);
		} catch (Exception e) {
			log.error("Error occur in Redis.keys, pattern=: " + patternstr + " exception:" + e);
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return result;
	}

	public void set(String key, String value) {
		set(key, value, -1);
	}

	public void addList(String key, String value) {
		setList(key, value, -1);
	}

	public List<String> getMValue(String[] udidList) {
		JedisPool pool = null;
		Jedis conn = null;
		List<String> ret = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			ret = conn.mget(udidList);
			log.debug("Redis-Get: Key=" + udidList.length + ",Value=" + ret);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s, error message: " + e.getMessage(),
					udidList.length));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return ret;
	}

	public String get(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		String ret = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			ret = conn.get(key);
			log.debug("Redis-Get: Key=" + key + ",Value=" + ret);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s, error message: " + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}

		return ret;
	}

	public List<String> getList(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		List<String> ret = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			ret = conn.lrange(key, 0, -1);
			log.debug("Redis-Get: Key=" + key + ",Value=" + ret);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s, error message: " + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}

		return ret;
	}

	public Set<String> getSet(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		Set<String> ret = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			ret = conn.smembers(key);
			log.debug("Redis-Get: Key=" + key + ",Value=" + ret);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.set, key=%s, error message: " + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}

		return ret;
	}

	public void del(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.del(key);
			log.debug("Redis-Del: Key=" + key);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	public long delFromSet(String key, String member) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = -1;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.srem(key, member);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	public long delFromSortedSet(String key, String member) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = -1;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.zrem(key, member);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	public boolean isInSet(String key, String member) {
		JedisPool pool = null;
		Jedis conn = null;
		boolean flag = false;
		try {
			pool = getPool();
			conn = pool.getResource();
			flag = conn.sismember(key, member);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return flag;
	}

	public long getValueSize(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = 0;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.scard(key);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	public long getSortedSetValueSize(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = 0;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.zcard(key);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	/**
	 * 设置有效期，单位 秒
	 */
	@Override
	public long IncValue(String key, int expire) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = 0;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.incr(key);
			if (expire != -1)
				conn.expire(key, expire);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	public static long DecValue(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		long i = 0;
		try {
			pool = getPool();
			conn = pool.getResource();
			i = conn.decr(key);
			log.debug("Redis-Del: Key=" + key);

		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.del, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return i;
	}

	public boolean compareAndIncrBy(final String key, final String exp, final long incrValue) {
		JedisPool pool = null;
		Jedis conn = null;
		boolean ret = false;
		try {
			pool = getPool();
			conn = pool.getResource();

			conn.watch(key); // 监视当前的key
			String curr = conn.get(key);

			if (StringUtils.equals(exp, curr)) {
				Transaction tx = conn.multi();
				tx.incrBy(key, incrValue);
				List<Object> txResult = tx.exec();
				if (txResult != null && !txResult.isEmpty()) {
					// 修改成功
					ret = true;
				}
			}
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.compareAndIncrBy, key=%s, errMsg=%s", key, e.getMessage()), e);

			if (pool != null && conn != null) {
				conn.unwatch();
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
			ret = false;
		} finally {
			if (pool != null && conn != null) {
				conn.unwatch();
				pool.returnResource(conn);
			}
		}

		if (ret) {
			log.debug("Redis-compareAndIncBy: Key={}, success", key);
		} else {
			log.debug("Redis-compareAndIncBy: Key={}, fail", key);
		}

		return ret;
	}

	public boolean compareAndDecrBy(final String key, final String exp, final long decValue) {
		JedisPool pool = null;
		Jedis conn = null;
		boolean ret = false;
		try {
			pool = getPool();
			conn = pool.getResource();

			conn.watch(key); // 监视当前的key
			String curr = conn.get(key);

			if (StringUtils.equals(exp, curr)) {
				Transaction tx = conn.multi();
				tx.decrBy(key, decValue);
				List<Object> txResult = tx.exec();
				if (txResult != null && !txResult.isEmpty()) {
					// 修改成功
					ret = true;
				}
			}
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.compareAndDecrBy, key=%s, errMsg=%s", key, e.getMessage()), e);

			if (pool != null && conn != null) {
				conn.unwatch();
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
			ret = false;
		} finally {
			if (pool != null && conn != null) {
				conn.unwatch();
				pool.returnResource(conn);
			}
		}

		if (ret) {
			log.debug("Redis-compareAndDecrBy: Key={}, success", key);
		} else {
			log.debug("Redis-compareAndDecrBy: Key={}, fail", key);
		}

		return ret;
	}

	public String getHashSetValue(String key, String field) {
		JedisPool pool = null;
		Jedis conn = null;
		String result = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			result = conn.hget(key, field);

			log.debug("Redis-Hget: Key={}, field={}, value={}", key, field, result);
		} catch (Exception e) {
			log.error("Error occur in Redis.Hget, key={}, error message: {}", key, e.getMessage());
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return result;
	}

	public Long setHashSetValue(String key, String field, String value) {
		JedisPool pool = null;
		Jedis conn = null;
		Long result = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			result = conn.hset(key, field, value);

			log.debug("Redis-Hset: Key={}, field={}, value={}", key, field, result);
		} catch (Exception e) {
			log.error("Error occur in Redis.Hset, key={}, error message: {}", key, e.getMessage());
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return result;
	}

	@Override
	public void set(String key, int exp, Object obj) {
		set(key, (String) obj, exp);

	}

	@Override
	public OperationFuture<Boolean> delete(String key) {
		del(key);
		return null;
	}

	@Override
	public Map<String, Object> getByList(List<String> list) {
		Jedis jedis = getPool().getResource();
		Map<String, Object> map = New.hashMap();
		try {
			String[] keysArr = new String[list.size()];
			keysArr = list.toArray(keysArr);
			List<String> valueList = jedis.mget(keysArr);
			int i = 0;
			for (String key : list) {
				map.put(key, valueList.get(i));
				i++;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			if (jedis != null)
				pool.returnResource(jedis);
		}
		return map;
	}

	

	@Override
	public Long publish(String channel, String message) {
		JedisPool pool = null;
		Jedis conn = null;
		long publisResult = 0L;
		try {
			pool = getPool();
			conn = pool.getResource();
			publisResult = (conn.publish(channel, message));
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.publish, channel=%s,message=%s" + e.getMessage(), channel,
					message));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return publisResult;
	}

	@Override
	public void lpush(String key, String value) {
		JedisPool pool = null;
		Jedis conn = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			conn.lpush(key, value);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.lpush, key=%s,value=%s" + e.getMessage(), key, value));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
	}

	@Override
	public String lpop(String key) {
		JedisPool pool = null;
		Jedis conn = null;
		String value = null;
		try {
			pool = getPool();
			conn = pool.getResource();
			value = conn.lpop(key);
		} catch (Exception e) {
			log.error(String.format("Error occur in Redis.lpop, key=%s" + e.getMessage(), key));
			if (pool != null && conn != null) {
				pool.returnResource(conn);
				pool = null;
				conn = null;
			}
		} finally {
			if (pool != null && conn != null)
				pool.returnResource(conn);
		}
		return value;
	}

	@Override
	public Map<String, String> getHsetAll(String key) {
		logger.error("use wrong redis !!!");
		return null;
	}

	@Override
	public long incrBy(String key, int incNumber, int exp) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addHashSetValue(String yJkey, String field, int j) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sadd(String key, String members) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> sdiff(String s1, String s2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getHKeys(String key) {
		// TODO Auto-generated method stub
		return null;
	}

}
