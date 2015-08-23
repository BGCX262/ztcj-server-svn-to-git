package com.wm927.commons;

/**
 * 终端来源<默认来自中间件>
 * 根据前端发送连接的前缀判断是哪个终端发送来的请求
 * @author chen
 *
 */
public class ResponseCodeUtils {
	//来自安卓终端
	public final static String ANDROID_NAME = "Android";
	//来自安卓终端接口前缀
	public final static String ANDROID_PORT = "1";
	//来自苹果终端
	public final static String IPHONE_NAME = "Iphone";
	//来自苹果终端接口前缀
	public final static String IPHONE_PORT = "2";
	//来自WEB终端
	public final static String WEB_NAME = "web";
	//来自WEB终端接口前缀
	public final static String WEB_PORT = "3";
	//来自HTML网页终端
	public final static String HTML_NAME = "HTML";
	//来自HTML网页接口前缀
	public final static String HTML_PORT = "4";
	//来自电脑客户端终端
	public final static String PC_NAME = "PC";
	//来自电脑客户端接口前缀
	public final static String PC_PORT = "5";
	//来自中间件终端
	public final static String MIDDLE_NAME = "Middle";
	//来自中间件终端接口前缀
	public final static String MIDDLE_PORT = "6";
	//默认的终端号
	public final static String DEFAULT_PORT = "3";
	//默认返回数据格式json
	public final static String DEFAULT_MODE = "0";
	//默认返回数据格式json
	public final static String JSON__MODE = "0";
	//默认返回数据格式json
	public final static String XML_MODE = "1";
	//默认的版本号
	public final static String DEFAULT_VERSION = "001";
	/**
	 * 格式化终端请求
	 * @param port
	 * @return
	 */
	public static String responseMode(String port){
		if(DataUtils.checkString(port)){
			return MIDDLE_NAME;
		}
		if(port.indexOf(ANDROID_PORT)!=-1)
			port = ANDROID_NAME;
		else if(port.indexOf(IPHONE_PORT)!=-1)
			port = IPHONE_NAME;
		else if(port.indexOf(WEB_PORT)!=-1)
			port = WEB_NAME;
		else if(port.indexOf(HTML_PORT)!=-1)
			port = HTML_NAME;
		else if(port.indexOf(PC_PORT)!=-1)
			port = PC_NAME;
		else
			port = MIDDLE_NAME;
		return port;
	}
}
