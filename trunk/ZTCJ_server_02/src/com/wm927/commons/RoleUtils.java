package com.wm927.commons;

/**
 * 角色标识
 * @author chen
 * 具体内容请查看数据库表 wm_user_role
 *
 */
public class RoleUtils {
	/**
	 * 分析师角色id
	 */
	public static final String ANALYST_ROLE = "3";
	/**
	 * 机构角色id
	 */
	public static final String ORGANIZATION_ROLE = "4";
	/**
	 * 普通用户角色id
	 */
	public static final String GENERAL_ROLE = "2";
	/**
	 * 管理员角色id
	 */
	public static final String ADMIN_ROLE = "1";
	/**
	 * app分析师角色id
	 */
	public static final String APPANALYST_ROLE = "5";
	/**
	 * app机构角色id
	 */
	public static final String APPORGANIZATION_ROLE = "6";
	/**
	 * 默认的角色id
	 */
	public static final String DEFAULT_ROLE = "2";
	
	/**
	 * 验证分析师
	 * @param role
	 * @return
	 */
	public static  boolean checkAnalyst( String role){
		return ANALYST_ROLE.endsWith(role);
	}
	/**
	 * 验证机构
	 * @param role
	 * @return
	 */
	public static  boolean checkOrgnization( String role){
		return ORGANIZATION_ROLE.endsWith(role);
	}
	/**
	 * 验证普通用户
	 * @param role
	 * @return
	 */
	public static  boolean checkGeneral( String role){
		return GENERAL_ROLE.endsWith(role);
	}
	/**
	 * 验证管理员
	 * @param role
	 * @return
	 */
	public static  boolean checkAdmin( String role){
		return ADMIN_ROLE.endsWith(role);
	}
	/**
	 * 验证APP分析师
	 * @param role
	 * @return
	 */
	public static  boolean checkAppAnalyst( String role){
		return APPANALYST_ROLE.endsWith(role);
	}
	/**
	 * 验证APP机构
	 * @param role
	 * @return
	 */
	public static  boolean checkAppOrgnization( String role){
		return APPORGANIZATION_ROLE.endsWith(role);
	}
}
