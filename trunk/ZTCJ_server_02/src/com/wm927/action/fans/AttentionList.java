package com.wm927.action.fans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.ResponseCodeUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 关注列表
 */
public class AttentionList extends MiddlewareActionService{
	
	private  String page;
	private  String number;
	private  String uid;
	private  String ouid;
	private  String terminal  = ResponseCodeUtils.DEFAULT_PORT;
	/**
	 * 关注列表
	 * 如果传入只有uid代表获取个人关注列表
	 * 如果传入有ouid代表获取他人的关注列表
	 */
	public void execute(){
			if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户id为空"},new Object[]{uid,ouid}))
				return ; 
			if(!checkUser(uid))
				return ;
			String fansList = "SELECT FID AS UID FROM wm_user_friends WHERE UID = "+ouid;
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
	
	/**
	 * 关注的分析师列表,仅适用于IOS
	 * @return
	 */
	public void attentionAnalyst(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		String sql = "SELECT fri.FID  FROM wm_user_friends fri  LEFT JOIN wm_user_roleAnalyst role ON role.UID=fri.FID WHERE fri.UID = "+uid+" AND role.AUDITSTATE=1";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		if(list_value == null || list_value.isEmpty()){
			responseInfo("1","成功");
			return;
		}
		List<Map<String,Object> > list_value2 = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value2 = new HashMap<String,Object>();
		for(Map<String,Object> map : list_value){
			map_value2 = findUserInfo(map.get("fid"));
			map_value2.putAll(findUserComInfo(map.get("fid")));
			map_value2.put("attentionstate", attentionState(uid, map.get("fid")+""));
			list_value2.add(map_value2);
			
		}
		Map<String,Object> pageInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value2,pageInfo);
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


	public String getTerminal() {
		return terminal;
	}


	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	
	
	
}
