package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DES;
import com.wm927.service.impl.MiddlewareActionService;

public class SimplePointLogin extends MiddlewareActionService{
	private String mac;//max地址
	private String uid;//用户id
	private String time;//过期时间
	private String key ;
	
	/**
	 * 单点登录，修改用户相关信息
	 */
	public void execute(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(DataUtils.checkString(mac)){
			responseInfo("-1","mac地址不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "UPDATE wm_user_index SET MAC = ?,EXPIRESTIME = ?  WHERE ID = ?";
		String encMac = DES.GetDES(mac+uid);
		//设置过期时间，为当前时间加上30分钟
		long expiresTime = System.currentTimeMillis()/1000 + 30*60 ;
		int count = middlewareService.update(sql,encMac,expiresTime,uid);
		if(count == 0){
			responseInfo("-3","修改MAC地址失败");
			return;
		}
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("key", encMac);
		list_value.add(map_value);
		responseInfo("1","成功",list_value);
	}
	/**
	 * 在线状态延迟+30分钟
	 */
	public void updateExpiresTime(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(DataUtils.checkString(key)){
			responseInfo("-1","key不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "SELECT MAC , EXPIRESTIME FROM wm_user_index WHERE ID = ?";
		Map<String,Object> map = middlewareService.findFirst(sql,uid);
		
		if(!key.equals(map.get("mac"))){
			responseInfo("-3","MAC地址不对");
			return ;
		}
		Long dbtime ;
		try{
			dbtime = Long.parseLong(map.get("expirestime").toString());
		}catch(Exception e){
			responseInfo("-3","时间已经过期");
			return;
		}
		long nowTime = System.currentTimeMillis()/1000 ;
		if(nowTime > dbtime){
			responseInfo("-3","时间已经过期");
			return;
		}
		long nextTime = nowTime + 30*60;
		String updateSql = "UPDATE wm_user_index SET EXPIRESTIME = ?  WHERE ID = ?";
		int count = middlewareService.update(updateSql,nextTime,uid);
		if( count == 0){
			responseInfo("-3","更新MAC新过期时间失败");
			return;
		}
		responseInfo("1","更新MAC新过期时间成功");
	}
	/**
	 * 验证PC端MAC登录状态
	 */
	public void checkLoginState(){
		if(DataUtils.checkString(key)){
			responseInfo("-1","key不能为空");
			return;
		}
		String sql = "SELECT ID ,EXPIRESTIME FROM wm_user_index WHERE MAC = ?";
		Map<String,Object> map = middlewareService.findFirst(sql,key);
		if(map == null || map.isEmpty()){
			responseInfo("-3","不存在当前MAC地址的用户");
			return;
		}
		
		Long dbtime ;
		try{
			dbtime = Long.parseLong(map.get("expirestime").toString());
		}catch(Exception e){
			responseInfo("-3","时间已经过期");
			return;
		}
		long nowTime = System.currentTimeMillis()/1000 ;
		if(nowTime > dbtime){
			responseInfo("-3","时间已经过期");
			return;
		}
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("uid", map.get("id"));list_value.add(map_value);
		responseInfo("1","返回成功",list_value);
	}
	public String getMac() {
		return mac;
	}
	public void setMac(String mac) {
		this.mac = mac;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	
}
