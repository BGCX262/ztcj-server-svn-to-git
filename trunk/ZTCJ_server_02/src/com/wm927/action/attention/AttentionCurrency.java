package com.wm927.action.attention;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 显示关注货币，与关注货币接口
 * @author chen
 *
 */
public class AttentionCurrency   extends MiddlewareActionService{
	Logger log = Logger.getLogger(AttentionCurrency.class.getName());
	private String uid;
	private String cid;//货币ID
	private String type ;//0代表关注货币，1代表取消关注货币
	/**
	 * 显示关注的货币(偏好设置，不需要关注状态)
	 */
	public void execute() {
		String sql = "SELECT ID,CODE,NAME FROM wm_setting_currency WHERE ISOPEN  = 1 AND ISTOATTENTION = 1";
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo("1","成功",list_value);
	}
	/**
	 * 显示货币列表(显示个人关注资料，需要当前用户与货币的关注状态)
	 */
	public void showAttentionCurrency(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		String sql = "SELECT ID,CODE,NAME FROM wm_setting_currency WHERE ISOPEN  = 1 AND ISTOATTENTION = 1";
		List<Map<String,Object>> list_value1 = middlewareService.find(sql);
		String myAttentionCurrency = "SELECT CID FROM wm_user_attentionCurrency WHERE UID = ?";
		List<Map<String,Object>> list_value2 = middlewareService.find(myAttentionCurrency,uid);
		//利用SET集合不能插入重复元素，赛选出来
		//如果插入成功，代表当前元素不存在，则未关注，否则关注了
		Set<Object> set = new HashSet<Object>();
		for(Map<String,Object> map:list_value2){
			set.add(map.get("cid"));
		}
		for(Map<String,Object> map:list_value1){
			if(set.add(map.get("id"))){
				//未关注
				map.put("state", "0");
			}else{
				map.put("state", "1");
			}
		}
		responseInfo("1","成功",list_value1);
	}
	/**
	 * 关注货币，支持批量
	 */
	public void attentionCurrency(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String deleteCur = "DELETE FROM wm_user_attentionCurrency WHERE UID = ?";
		if(DataUtils.checkString(cid)){
			middlewareService.update(deleteCur,uid);
			responseInfo("1","取消关注货币成功");
			return;
		}
		
		String cids [] = cid.split(",");
		String sql = "INSERT INTO wm_user_attentionCurrency(UID,CID)VALUES(?,?)";
		middlewareService.update(deleteCur,uid);
		for(String c :cids ){
			middlewareService.update(sql,uid,c);
		}
		responseInfo("1","关注货币成功");
	}
	/**
	 * 单个国家关注
	 */
	public void attentionSimpleCurrency(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(DataUtils.checkString(cid)){
			responseInfo("-1","关注的货币ID不能为空");
			return;
		}
		if(DataUtils.checkString(type)){
			responseInfo("-1","关注货币状态不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "INSERT INTO wm_user_attentionCurrency(UID,CID)VALUES(?,?)";
		String msg = "关注货币成功";
		String checkCountry = "SELECT ID FROM wm_setting_currency WHERE ID = ?";
		
		String countid  = middlewareService.findBy(checkCountry,"ID",cid);
		if(DataUtils.checkString(countid)){
			responseInfo("-1","关注的货币不存在");
			return;
		}
		if("1".equals(type)){
			sql = "DELETE FROM wm_user_attentionCurrency WHERE UID = ? AND CID = ? ";
			msg = "取消关注国家成功";
		}else{
			//验证国家是否已经关注，已经关注则不能继续关注
			String checkAttentionCountry = " SELECT COUNT(*) FROM wm_user_attentionCurrency WHERE UID = ? AND CID = ?";
			Long count2  = middlewareService.findCount(checkAttentionCountry,uid,cid);
			if(count2 > 0){
				responseInfo("-1","货币已经关注");
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
