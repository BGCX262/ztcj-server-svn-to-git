package com.wm927.dbutils;

import java.util.HashMap;
import java.util.Map;

import com.wm927.service.impl.MiddlewareActionService;

public class DBTest extends MiddlewareActionService{

	public  void execute(){
		String sql = "SELECT * FROM wm_user_index WHERE ID=88888";
		Map<String,Object> map = middlewareService.findFirst(sql);
		System.out.println(map);
	}
	public static void main(String args[]){
		Map<String,String>  map = new HashMap<String,String>();
		System.out.println(map.put("a", "1"));
		System.out.println(map.put("a", "2"));
		System.out.println(map.remove("b"));
	}
}
