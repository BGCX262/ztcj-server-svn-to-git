package com.wm927.action.attention;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 国家地区接口
 * @author chen
 *
 */
public class AttentionArea  extends MiddlewareActionService {
	private String uid;
	private String cid;//国家ID
	private String type;//0代表关注国家，1代表取消关注国家
	/**
	 * 显示国家列表(偏好设置，不需要当前用户与国家的关注状态)
	 */
	public void execute() {
		String sql = "SELECT ID,AREANAME,AREACODE FROM wm_setting_area WHERE ISTOATTENTION  = 1";
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo("1","成功",list_value);
	}
	/**
	 * 显示国家列表(显示个人关注资料，需要当前用户与国家的关注状态)
	 */
	public void showAttentionCountry(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		String sql = "SELECT ID,AREANAME,AREACODE FROM wm_setting_area WHERE ISTOATTENTION  = 1";
		List<Map<String,Object>> list_value1 = middlewareService.find(sql);
		String myAttentionCountry = "SELECT CID FROM wm_user_attentionCountry WHERE UID = ?";
		List<Map<String,Object>> list_value2 = middlewareService.find(myAttentionCountry,uid);
		Set<Object> set = new HashSet<Object>();
		//利用SET集合不能插入重复元素，赛选出来
		//如果插入成功，代表当前元素不存在，则未关注，否则关注了
		for(Map<String,Object> map:list_value2){
			set.add(map.get("cid"));
		}
		for(Map<String,Object> map:list_value1){
			if(set.add(map.get("id"))){
				map.put("state", "0");
			}else{
				map.put("state", "1");
			}
		}
		responseInfo("1","成功",list_value1);
	}
	/**
	 * 关注国家(单个，多个)
	 * @return
	 */
	public void attentionCountry(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String deleteCountry = "DELETE  FROM wm_user_attentionCountry WHERE UID = ?";
		if(DataUtils.checkString(cid)){
			//取消所有关注国家
			middlewareService.update(deleteCountry,uid);
			responseInfo("1","取消关注国家成功");
			return;
		}
		
		String cids [] = cid.split(",");
		String sql = "INSERT INTO wm_user_attentionCountry(UID,CID)VALUES(?,?)";
		middlewareService.update(deleteCountry,uid);
		for(String c :cids ){
			middlewareService.update(sql,uid,c);
		}
		responseInfo("1","关注国家成功");
		
	}
	/**
	 * 单个国家关注
	 */
	public void attentionSimpleCountry(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(DataUtils.checkString(cid)){
			responseInfo("-1","关注的国家ID不能为空");
			return;
		}
		if(DataUtils.checkString(type)){
			responseInfo("-1","关注国家状态不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "INSERT INTO wm_user_attentionCountry(UID,CID)VALUES(?,?)";
		String msg = "关注国家成功";
		String checkCountry = "SELECT ID FROM wm_setting_area WHERE ID = ?";
		
		String countid  = middlewareService.findBy(checkCountry,"ID",cid);
		if(DataUtils.checkString(countid)){
			responseInfo("-1","关注的国家不存在");
			return;
		}
		if("1".equals(type)){
			sql = "DELETE FROM wm_user_attentionCountry WHERE UID = ? AND CID = ? ";
			msg = "取消关注国家成功";
		}else{
			//验证国家是否已经关注，已经关注则不能继续关注
			String checkAttentionCountry = " SELECT COUNT(*) FROM wm_user_attentionCountry WHERE UID = ? AND CID = ?";
			Long count2  = middlewareService.findCount(checkAttentionCountry,uid,cid);
			if(count2 > 0){
				responseInfo("-1","国家已经关注");
				return;
			}
		}	
		
		middlewareService.update(sql,uid,cid);
		responseInfo("1",msg);
		
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
