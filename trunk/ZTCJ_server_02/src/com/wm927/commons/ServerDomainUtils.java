package com.wm927.commons;

import java.util.HashMap;
import java.util.Map;

public class ServerDomainUtils {
	public static Map<String,Object> map_value = new HashMap<String,Object>();
	/**
	 * 分析师主页地址
	 */
	public static final String ANALYST = "1";
	public static final String ANALYST_URI = "http://home.wm927.hk/user/info/index?uid=";
	
	/**
	 * APP下载路径
	 */
	public static final String APP_DOWN = "appDown";
	public static final String APP_DOWN_URI = ServerDomainUtils.class.getResource("/").getPath() +"app/";
	
	/**
	 * APP上传路径
	 */
	public static final String APP_UP = "appUp";
	public static final String APP_UP_URI = "/webapp/appapk/andriod/";
	/**
	 * APP服务器域名
	 */
	public static final String SERVER_DOMAIN_APP = "appBaseUrl";
	public static final String SERVER_DOMAIN_URI_APP = "http://down.wm927.com";
	/**
	 * 图片服务器域名
	 */
	public static final String SERVER_DOMAIN = "baseUrl";
	public static final String SERVER_DOMAIN_URI = "http://imgcdn.wm927.com/sns/";
	/**
	 * 用户头像
	 */
	public static final String PHOTO = "userimgUrl";
	public static final String PHOTO_URI = "userimg/";
	/**
	 * app桌面icon
	 */
	public static final String ICON = "appiconUrl";
	public static final String ICON_URI = "appimg/icon/";
	/**
	 * app自动生成头像
	 */
	public static final String APP = "appavatarUrl";
	public static final String APP_URI = "appimg/avatar/";
	
	/**
	 * 博客图片
	 */
	public static final String BLOG = "blogimgUrl";
	public static final String BLOG_URI = "blogimg/";
	
	static {
		map_value.put(SERVER_DOMAIN, SERVER_DOMAIN_URI);
		map_value.put(PHOTO, PHOTO_URI);
		map_value.put(ICON, ICON_URI);
		map_value.put(APP, APP_URI);
		map_value.put(BLOG, BLOG_URI);
		map_value.put(SERVER_DOMAIN_APP, SERVER_DOMAIN_URI_APP);
	}
}
