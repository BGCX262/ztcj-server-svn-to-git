package com.wm927.commons;


/**
 * 将全局常量放入此
 */
public class Contants {
	/**
	 * 公司默认的机构OID
	 */
	public static final String DEFAULT_OID = "1352";
	/**
	 * 登录默认的MD5加密KEY
	 */
	public static final String DEFAULT_MD5_LOGIN_KEY = "-ztFinance_er@mail.com";
	/**
	 * 公司MD5加密KEY
	 */
	public static final String DEFAULT_MD5_KEY = "wm927";
	/**
	 * 单引号常量
	 */
	public static final String SINGLE_MARK="''";
	/**
	 * 双引号常量
	 */
	public static final String DOUBLE_MARK="\"\"";
	/**
	 * 内盘
	 */
	public static final String INNER_DISK = "Au(T+D),Ag(T+D),TJAP,TJMAG,TJMAP,TJNI,TJMN," +
								    "TJAG,TJPD,TJMP,SGAu9999,SGAuT+D,SGAgT+D,AG15,AG100,AG1,AU,AG,Ag99.99,Au99.99";
	/**
	 * 外盘
	 */
	public static final String OUTER_DISK = "USD,EURUSD,USDJPY,GBPUSD,USDCHF,AUDUSD,USDCAD," +
											"NZDUSD,EURJPY,EURGBP,EURCHF,GBPJPY,GBPCHF,AUDJPY,AUDCAD,NZDJPY," +
											"XAU,XAG,COMEXAU,COMEXAG,XPD,XPT";
	/**
	 * 直盘
	 */
	public static final String STRAIGHT_DISK = "USD,EURUSD,USDJPY,GBPUSD,USDCHF,AUDUSD,USDCAD,NZDUSD";
	/**
	 * 交叉盘
	 */
	public static final String CROSS_DISK = "EURJPY,EURGBP,EURCHF,GBPJPY,GBPCHF,AUDJPY,AUDCAD,NZDJPY";
	/**
	 * 黄金白银
	 */
	public static final String GOLD = "Au(T+D),Ag(T+D),XAU,XAG,COMEXAU,COMEXAG,XPD,XPT,Ag99.99,Au99.99"+OUTER_DISK; 
	
}
