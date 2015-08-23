package com.wm927.interceptor;


import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.wm927.dbutils.ContextHolder;


public class DatabaseInterceptor implements MethodInterceptor{

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		String methodName = invocation.getMethod().getName();
		//所有以find操作的都放入从库操作
		if(methodName.startsWith("find")){
			//若此处有多个从库，可以自定义数据源字符串，然后使用随机数拼接数据源
			ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);
		}else{
			ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE);
		}
		
		return invocation.proceed();
	}


}
