package com.wm927.exception;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.wm927.service.impl.MiddlewareActionService;

/**
 *前天发送错误请求返回的消息 
 * @author chen
 *
 */
public class ErrorAction extends MiddlewareActionService{
	public void execute(){
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("statusCode", "-1");
		map_value.put("data", new ArrayList<String>());
		map_value.put("statusMsg", "请求发送的路径不正确，请确认后再发送！！");
		responseToHtml(responseToJsonObject(map_value));
	}
}
