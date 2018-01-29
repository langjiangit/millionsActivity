package com.bus.chelaile.model;



public enum PropertiesName {
    PUBLIC("public"),  //通用
    CACHE("cache");   //缓存
    
    private String type;
  //  private String val;
    
    private PropertiesName(String type) {
        this.type = type;
    }
    
    public String getType(){
    	return type;
    }
    
    public String getValue() {
        return type;
    }
    

}
