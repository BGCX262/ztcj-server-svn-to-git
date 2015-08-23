package com.wm927.action.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wm927.service.impl.MiddlewareActionService;

//http://192.168.0.53/server/301203.action 
//http://localhost:8080/Middleware_server/301203.action
public class TurnTableAction   extends MiddlewareActionService {
	
	//获取9条数据
	public void  execute(){
		String analystSql = "SELECT UID FROM wm_user_roleAnalyst WHERE ISTOTURNTABLE = 1 AND AUDITSTATE = 1 ORDER BY TURNTABLESORTVALUE DESC  LIMIT 9";
		List<Map<String,Object>> list_value = middlewareService.find(analystSql);
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map : list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
			map_value = findUserCallInfo(map.get("uid"));
			map.putAll(map_value);
			map_value = findUserComInfo(map.get("uid"));
			map.putAll(map_value);
			}
		responseInfo("1","成功",list_value);
		}
	}
