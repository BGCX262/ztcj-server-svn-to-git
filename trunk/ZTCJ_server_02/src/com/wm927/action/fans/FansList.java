package com.wm927.action.fans;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.ResponseCodeUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 粉丝列表
 * 测试接口http://192.168.0.53/server/30734.action?uid=33&pageindex=1&pagesize=2
 * //测试接口http://localhost:8080/Middleware_server/30734
 */
public class FansList extends MiddlewareActionService{
	
	private  String page;
	private  String number;
	private  String uid;
	private  String ouid;
	private  String terminal  = ResponseCodeUtils.DEFAULT_PORT;
	/**
	 * 返回粉丝列表
	 * 普通传入uid代表获取个人的粉丝列表
	 * 传入ouid代表获取他人的粉丝列表
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户id为空"},new Object[]{uid,ouid}))
			return ; 
		if(!checkUser(uid))
			return;
		String fansList = "SELECT  UID FROM wm_user_friends WHERE FID = "+ouid;
		List<Map<String,Object>> list_value = findPageInfo(page, number, fansList);
		Map<String,Object> map_value = new HashMap<String,Object>();
		
		for(Map<String,Object> map:list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
			map_value = findUserComInfo(map.get("uid"));
			map.putAll(map_value);
			map.put("state", attentionState(uid,map.get("uid")+""));//获取关注状态
			
		}
		Map<String,Object> pageInfoMap = findPageSize( page, number,fansList);
		responseInfo("1","返回数据成功",list_value,pageInfoMap);
}

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}

	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}
	
}
