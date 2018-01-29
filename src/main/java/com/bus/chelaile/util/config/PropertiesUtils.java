package com.bus.chelaile.util.config;



import java.io.IOException;
import java.util.Map;


/**
 * 配置文件读取
 * 
 * @author zzz
 * 
 */
public class PropertiesUtils extends PropertiesManager{
	
	public static String getValue(String propertiesName,String key,String defaultValue){
		Map<String,String> tempMap=getMap().get(propertiesName);
		if(tempMap==null||tempMap.size()==0){
			return defaultValue;
		}else{
			if( !tempMap.containsKey(key) ){
				return defaultValue;
			}
		}
		return tempMap.get(key);
	}

	
	public static String getValue(String propertiesName,String key){
		Map<String,String> tempMap=getMap().get(propertiesName);
		if(tempMap==null||tempMap.size()==0){
			return null;
		}
		return tempMap.get(key);
	}




	public static void main(String[] args) throws IOException {
		//PropertiesManager.init();
		System.out.println(PropertiesUtils.getValue("public", "filePath",""));
	}

}



