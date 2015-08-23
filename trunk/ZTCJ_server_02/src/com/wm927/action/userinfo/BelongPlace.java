package com.wm927.action.userinfo;

import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 所在地列表
 * @author chen
 * 修改于2013-10-22
 */
public class BelongPlace extends MiddlewareActionService{
	//类型id，代表是国还是省还是市区
	private String typeid;
	//父类id，代表所属什么位置下的地区
	private String parentid;
	/**
	 * 所在列表
	 * 第一步：判断字段是否为空
	 * 第二步：根据父类ID与级别ID获取当前的地区信息
	 */
	public void execute(){
		if(DataUtils.checkString(typeid)){
			responseInfo("-1","类型ID不能为空");
			return;
		}
		if(DataUtils.checkString(parentid)){
			responseInfo("-1","父类ID不能为空");
			return;
		}
		String sql = "SELECT ID,PARENTID,AREANAME,LEVEL FROM wm_setting_area WHERE LEVEL = ? AND PARENTID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,typeid,parentid);
		responseInfo("1","获取地区信息成功",list_value);
		}
	
	public String getTypeid() {
		return typeid;
	}

	public void setTypeid(String typeid) {
		this.typeid = typeid;
	}

	public String getParentid() {
		return parentid;
	}

	public void setParentid(String parentid) {
		this.parentid = parentid;
	}
}
