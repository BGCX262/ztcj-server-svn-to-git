package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 认证结果
 * @author chen
 * 修改于2013-10-22
 */
public class AuthenticateResult extends MiddlewareActionService{
	//用户id
	private String uid;

	/**
	 * 认证结果
	 * 第一步：判断用户id是否存在
	 * 第二步：判断用户是否存在
	 * 第三步：检查机构认证表是否有认证记录
	 * 第四步：检查分析师认证表是否有认证记录
	 */
	public void execute(){
		Map<String,Object> map_value = new HashMap<String,Object>();
		if(DataUtils.checkString(uid)){
			responseInfo("1","用户ID不能为空");
		}
		if(!checkUser(uid)){
			return;
		}
		//判断机构认证表是否有申请认证
		String agencySql = "SELECT AUDITSTATE FROM wm_user_roleOrganization WHERE UID = ?";
		String  agencyObj = middlewareService.findBy(agencySql,"AUDITSTATE",uid);
		//这个字段进行两重作用：1，如果为空，则代表没有机构认证申请，如果不为空则可以判断是否通过
		if(DataUtils.checkString(agencyObj)){
			map_value.put("agency", "0");
		}else{
			map_value.put("agency", "1");
			//判断审核是否通过
			map_value.put("ispass", agencyObj);
		}
		
		//判断分析师认证是否有申请认证
		String analystSql = "SELECT AUDITSTATE FROM wm_user_roleAnalyst WHERE UID = ?";
		String  analystsObj = middlewareService.findBy(analystSql,"AUDITSTATE",uid);
		if(DataUtils.checkString(analystsObj)){
			map_value.put("analysts", "0");
		}else{
			map_value.put("analysts", "1");
			//判断审核是否通过
			map_value.put("ispass", analystsObj);
		}
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		list_value.add(map_value);
		responseInfo("1","认证结束",list_value);
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
