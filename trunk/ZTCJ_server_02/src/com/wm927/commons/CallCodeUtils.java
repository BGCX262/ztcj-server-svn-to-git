package com.wm927.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CallCodeUtils {
	public static final Map<String, String> CODE_VALUE = new HashMap<String,String>();
	private static Logger logger = Logger.getLogger(CallCodeUtils.class);
	static {
		SAXReader saxReader = new SAXReader();
		Document document = null;
		
		try {
			String path = CallCodeUtils.class.getResource("/").getPath()+"CodeList.xml";
			document = saxReader.read(path);
		} catch (DocumentException e) {
			logger.info(e.getMessage());
		}
		@SuppressWarnings("rawtypes")
		List list_name = document.selectNodes("/rmds/request/item/name");
		@SuppressWarnings("rawtypes")
		Iterator it_nameType = list_name.iterator();
		String money_code = "";
		String money_value = "";
		while (it_nameType.hasNext()) {
			Element el = (Element) it_nameType.next();
			money_code = el.attributeValue("own_code");
			money_value = el.attributeValue("dec");
			CODE_VALUE.put(money_code, money_value);
		}
	}
	public static void main(String args[]){
		
		System.out.println(CallCodeUtils.CODE_VALUE);
	}
}
