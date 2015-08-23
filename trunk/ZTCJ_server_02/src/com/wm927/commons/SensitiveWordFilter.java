package com.wm927.commons;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 敏感词汇过滤器
 * 郭瑜嘉
 * 2013/11/13
 * @version 1.0
 */
public class SensitiveWordFilter{
	private static Pattern pattern = null;
	
	public SensitiveWordFilter() {
		StringBuffer patternBuf = new StringBuffer("");
		try {
			InputStream in = SensitiveWordFilter.class.getClassLoader().getResourceAsStream("SensitiveWord.properties");
			Properties pro = new Properties();
			pro.load(in);
			Enumeration enu = pro.propertyNames();
			while (enu.hasMoreElements()) {
				patternBuf.append((String) enu.nextElement() );
			}
			// unix换成UTF-8
			// pattern = Pattern.compile(new
			// String(patternBuf.toString().getBytes("ISO-8859-1"), "UTF-8"));
			// win下换成gb2312
			//pattern = Pattern.compile(new String(patternBuf.toString().getBytes("ISO-8859-1"), "UTF-8"));
			pattern = Pattern.compile(patternBuf.toString());
		} catch (IOException ioEx) {
			ioEx.printStackTrace();
		}
	}
	
	public static String doFilter(String str) {
		Matcher m = pattern.matcher(str);
		str = m.replaceAll("*");
		return str;
	}
}
