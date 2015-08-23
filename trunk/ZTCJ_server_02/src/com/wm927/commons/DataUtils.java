package com.wm927.commons;

public class DataUtils {
	/**
	 * 单引号常量
	 */
	public static final String SINGLE_MARK="''";
	/**
	 * 双引号常量
	 */
	public static final String DOUBLE_MARK="\"\"";
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean checkString(Object str){
		if(str==null||"".equals(str))
			return true;
		return SINGLE_MARK.equals(str)||DOUBLE_MARK.equals(str);
	}
	/**
	 * 验证整数
	 * @param str
	 * @param defaultNumber
	 * @return
	 */
	public static int praseNumber(String str , int defaultNumber){
		int number = defaultNumber;
		if(str==null||"".equals(str))
			return number;
		try{
			number = Integer.parseInt(str);
		}catch(Exception e){
			number = defaultNumber;
		}
		return number;
	}
	public static int praseNumber(String str ){
		return praseNumber(str,0);
	}
	
}
