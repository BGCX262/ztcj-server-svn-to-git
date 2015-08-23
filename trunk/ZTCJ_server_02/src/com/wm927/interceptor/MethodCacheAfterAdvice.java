package com.wm927.interceptor;

import java.util.List;
import net.sf.ehcache.Cache;
import org.apache.log4j.Logger;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.beans.factory.InitializingBean;

/**
 * AOP事务，更新缓存
 * @author chen
 * 已废弃
 */
public class MethodCacheAfterAdvice implements AfterReturningAdvice,InitializingBean{

	private static final Logger logger = Logger.getLogger(MethodCacheAfterAdvice.class); 
 
    private Cache cache;

    public void setCache(Cache cache) {
		 this.cache = cache;
	  }

    public MethodCacheAfterAdvice() {
		 super();
    }

    public void afterPropertiesSet() throws Exception {

	}

	public void afterReturning(Object returnValue, java.lang.reflect.Method method,
			Object[] args, Object classObj) throws Throwable {
		String className = classObj.getClass().getName();
		System.out.println(className);
		System.out.println(args);
		System.out.println(returnValue);
		System.out.println(method);
	    @SuppressWarnings("rawtypes")
		List list = cache.getKeys();
	    String cacheKey = "";
	    for (int i = 0; i < list.size(); i++) {
			  cacheKey = String.valueOf(list.get(i));
		      if (cacheKey.startsWith(className)) {
			    cache.remove(cacheKey);
			    logger.debug("remove cache " + cacheKey);
			  }
		 }
	 }
}

