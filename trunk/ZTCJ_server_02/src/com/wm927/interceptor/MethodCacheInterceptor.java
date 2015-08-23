package com.wm927.interceptor;

import java.io.Serializable;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * AOP事务 获取与保存缓存
 * @author chen
 * 已废弃
 */
public class MethodCacheInterceptor implements MethodInterceptor,InitializingBean {
	
	private static final Logger logger = Logger.getLogger(MethodCacheInterceptor.class);
	private Cache cache;
 
	public void setCache(Cache cache) {
	  this.cache = cache;
	}
	
	public MethodCacheInterceptor() {
	  super();
	  System.out.println("111");
	  logger.debug("CACHE IS START ----->>>>"+this.getClass().getSimpleName());
	}
	
	 /**
	  * 
	  * 
	  * 拦截Service/DAO的方法，并查找该结果是否存在，如果存在就返回cache中的值， 否则，返回数据库查询结果，并将查询结果放入cache
	  */
	 public Object invoke(MethodInvocation invocation) throws Throwable {
	  String targetName = invocation.getThis().getClass().getName();
	  String methodName = invocation.getMethod().getName();
	  Object[] arguments = invocation.getArguments();
	  Object result;
	  
	  logger.debug("Find object from cache is " + cache.getName());
	  String cacheKey = getCacheKey(targetName, methodName, arguments);
	  System.out.println(cacheKey);
	  Element element = cache.get(cacheKey);
	  if (element == null) {
	   logger
	     .debug("Hold up method , Get method result and create cache........!");
	   result = invocation.proceed();
	   element = new Element(cacheKey, (Serializable) result);
	   cache.put(element);
	  }
	  return element.getValue();
	 }

	 /**
	  * 
	  * 
	  * 获得cache key的方法，cache key是Cache中一个Element的唯一标识 cache key包括
	  * 包名+类名+方法名，如com.wm927.Service.MiddlewareServiceImpl.find
	  */
	 private String getCacheKey(String targetName, String methodName,
	   Object[] arguments) {
	  StringBuffer sb = new StringBuffer();
	  sb.append(targetName).append(".").append(methodName);
	  if ((arguments != null) && (arguments.length != 0)) {
	   for (int i = 0; i < arguments.length; i++) {
	    sb.append(".").append(arguments[i]);
	   }
	  }
	  return sb.toString();
	 }

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(cache, "Need a cache, please setCache(cache) create it");
		
	}
	
}
