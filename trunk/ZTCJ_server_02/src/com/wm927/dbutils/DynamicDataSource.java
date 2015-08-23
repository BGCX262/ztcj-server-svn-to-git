package com.wm927.dbutils;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
/**
 * 动态数据源
 * @author 郭瑜嘉
 * 2013/10/24
 * 2013/11/11 停用，改双dao写法
 */
public class DynamicDataSource extends AbstractRoutingDataSource{
  
    @Override  
    protected Object determineCurrentLookupKey() {
        return ContextHolder.getCustomerType();  
    }  
}  
