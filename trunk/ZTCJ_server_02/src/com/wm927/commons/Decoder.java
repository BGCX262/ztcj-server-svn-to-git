package com.wm927.commons;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class Decoder {
	/**
	 * 解码
	 * @param str
	 * @return
	 */
	public static String decode(String str){
		try {
			//此处是因为获取GET请求时，无法设置编码方式,TOMCAT默认编码是ISO8859-1,所以需要先将数据从ISO8859-9转成正常的UTF-8然后再decode
			//POST请求会设置编码，所以此处本无需转换编码，而且转换了会出现乱码，但是为了统一处理，所以需要前端encode数据一次
			//就算是转码了也不会影响
			str = new String( str.getBytes( "ISO8859-1" ),  "UTF-8" );
			str = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}
	public static String decode(String str,String charset){
		try {
			str = URLDecoder.decode(str, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;
	}
}
