package com.wm927.commons;

/**
 * 功能模块
 * @author chen
 * 具体功能请查看数据库表 wm_user_function(社区功能表) wm_role_function(社区功能也角色之间关系表) wm_user_role(社区角色表)
 * 主要操作步骤：
 * 1,根据uid查询当前用户的角色
 * 2,根据表中的角色在对应的类里面查找相对应的标识如：(CALL_FUCTION = "1")
 * 3,根据两个id去匹配wm_role_function表中是否存在当前权限
 */
public class FunctionUtils {
	/**
	 * 喊单功能ID
	 */
	public static final String CALL_FUCTION = "1";
	/**
	 * 申请认证
	 */
	public static final String REGIST_FUCTION = "2";
	/**
	 * 分析师VIP
	 */
	public static final String ANALYSTVIP_FUCTION = "3";
	/**
	 * 机构功能
	 */
	public static final String ORGANIZATION_FUCTION = "4";
	/**
	 * APP在线答疑&APP实战圈子
	 */
	public static final String APPCIRCLE_FUCTION = "5";
	/**
	 * 对权限的开放
	 */
	public static final String IS_OPEN = "1";
	/**
	 * 对权限的关闭
	 */
	public static final String IS_CLOSE = "0";
}
