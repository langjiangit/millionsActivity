package com.bus.chelaile.util.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import com.bus.chelaile.util.New;




public class PropertiesManager {
	// 保存父类的properties
	protected static Map<String, Map<String,String>> map = new HashMap<String, Map<String,String>>();
	
	// 文件路径
	private static final String propertiesPath = "/servicebiz/";
	// 子路经
	private static final String childPropertiesPath = "properties/";
	// 需要加载的配置文件
	private static final String propertiesTotalName = "systemtotal";
	// 配置文件名称
	private static final String fileName = ".properties";

	public static boolean initSuccess = false;

	static {
		// 启动定时加载配置文件
		//startThread();
		try {
			PropertiesManager.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void init() throws IOException {
		try {
			// 加载父类的所有属性
			appendParentToMap();
			// 加载子类的所有属性
			appendChildToMap();
			
			initSuccess = true;

		} catch (IOException e) {
			throw e;
		}		
	}
	
	private static void appendParentToMap() throws IOException{
		Properties	p = readProperty(propertiesPath+propertiesTotalName);
		appendToMap(p,propertiesTotalName);
	}
	
	private static List<String> getChildProperties(String key){
		String childConfig=map.get(propertiesTotalName).get(key);
		if(childConfig==null||childConfig.equals("")){
			return null;
		}
		String [] argChild = childConfig.split(",");
		List<String> list = New.arrayList();
		for( String child : argChild ){
			list.add(child);
		}
		return list;
	}
	

	private static void appendChildToMap() throws IOException{
		List<String> childList = getChildProperties("childConfig");
		for( String childProperties : childList ){
			Properties p = readProperty(propertiesPath+childPropertiesPath+childProperties);
			appendToMap(p,childProperties);
		}
	}
	
	/**
	 * 取得properties配置文件
	 * 
	 * @param properties
	 * @throws IOException 
	 */
	protected static Properties readProperty(String propertiesName) throws IOException {
		Properties props = new Properties();
		InputStream stream = null;
		try {
			stream = PropertiesUtils.class.getResourceAsStream(propertiesName+fileName);
			props.load(stream);
			return props;
		} catch (IOException e) {
			throw e;
			
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 加入到map里面
	 * 
	 * @param map
	 * @param properties
	 * @param propertiesName,
	 */
	private static void appendToMap(Properties properties,String propertiesName) {
		if (properties == null || properties.size() == 0) {
			return;
		}
		Set<Entry<Object, Object>> set = properties.entrySet();
		Iterator<Entry<Object, Object>> it = set.iterator();
		Map<String,String> valueMap = New.hashMap();
		while (it.hasNext()) {
			Entry<Object, Object> obj = it.next();
			valueMap.put(obj.getKey() + "", obj.getValue() + "");
		}
		map.put(propertiesName, valueMap);
	}

	/**
	 * 启动线程
	 */
	private static void startThread() {
		PropertiesUtilsThread tt = new PropertiesUtilsThread();
		Thread t = new Thread(tt);
		t.start();
	}

	protected static Map<String,Map<String,String>> getMap(){
		return map;
	}
}
	

/**
 * 线程加载配置文件
 * 
 * @author yuanxiang
 * 
 */
class PropertiesUtilsThread extends Thread {
	// 多长时间重现加载一次配置文件
	private static final int THREAD_TIME = 1000;// * 60 * 10;
	public void run() {
		while (true) {
			try {
				PropertiesManager.init();
				Thread.sleep(THREAD_TIME * 60);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if( !PropertiesManager.initSuccess ){
				continue;
			}
			try {
				PropertiesManager.init();
			} catch (IOException e) {
				//ErrorLog.wirteLog("run","PropertiesManager", "读取配置文件出错",e);
				//ErrorLog.wirteLog(e,"读取properties配置文件出错");
				
			}
		}

	}
}
