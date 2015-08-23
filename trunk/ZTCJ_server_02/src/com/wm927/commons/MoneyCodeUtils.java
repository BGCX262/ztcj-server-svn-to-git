package com.wm927.commons;

import java.util.HashMap;
import java.util.Map;



public class MoneyCodeUtils {
	public static final Map<String,String> CODE_MAP = new HashMap<String,String>();
	
	static {
		//内盘
		CODE_MAP.put("Au(T+D)", "TD黄金");
		CODE_MAP.put("Ag(T+D)", "TD白银");
		CODE_MAP.put("Ag99.99", "99银");
		CODE_MAP.put("Au99.99", "99金");
		//外盘
		CODE_MAP.put("USD", "美元指数");
		CODE_MAP.put("EURUSD", "欧元美元");
		CODE_MAP.put("USDJPY", "美元日元");
		CODE_MAP.put("GBPUSD", "英镑美元");
		CODE_MAP.put("USDCHF", "美元瑞郎");
		CODE_MAP.put("AUDUSD", "澳元美元");
		CODE_MAP.put("USDCAD", "美元加元");
		CODE_MAP.put("NZDUSD", "纽元美元");
		CODE_MAP.put("EURJPY", "欧元日元");
		CODE_MAP.put("EURGBP", "欧元英镑");
		CODE_MAP.put("EURCHF", "欧元瑞郎");
		CODE_MAP.put("GBPJPY", "英镑日元");
		CODE_MAP.put("GBPCHF", "英镑瑞郎");
		CODE_MAP.put("AUDJPY", "澳元日元");
		CODE_MAP.put("AUDCAD", "澳元加元");
		CODE_MAP.put("NZDJPY", "纽元日元");
		CODE_MAP.put("XAU", "现货黄金");
		CODE_MAP.put("XAG", "现货白银");
		
		CODE_MAP.put("AG15", "粤贵银9995");
		CODE_MAP.put("TJAG", "天津白银");
		CODE_MAP.put("AG", "沪银1406");
		CODE_MAP.put("AU", "沪金1406");
	}
}
