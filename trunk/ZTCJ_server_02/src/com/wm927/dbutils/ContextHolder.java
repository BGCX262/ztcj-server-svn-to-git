package com.wm927.dbutils;
/**
 * 动态数据源
 * @author 郭瑜嘉
 * 继续沿用动态数据源，这里做配置时候一定要包dao层作为唯一实例，不能是单列模式
 * 防止后期开发出现频繁出现使用ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);
 * 					  ContextHolder.clearCustomerType();
 * 我们需要做一种动态注解解释器，
 */
public class ContextHolder {
	public static final String DATA_SOURCE = "dataSource";//社区MYSQL1
    
    public static final String DATA_SOURCE2 = "dataSource2";//社区MYSQL2
      
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();  
      
    public static void setCustomerType(String customerType) {
    	
        contextHolder.set(customerType);  
    }  
      
    public static String getCustomerType() { 
        return contextHolder.get();  
    }  
      
    public static void clearCustomerType() {  
        contextHolder.remove();  
    }
}
