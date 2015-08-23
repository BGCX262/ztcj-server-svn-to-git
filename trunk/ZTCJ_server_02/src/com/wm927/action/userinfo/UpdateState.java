package com.wm927.action.userinfo;

import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 修改粉丝变动数，赞数，通知，评论数,在线答疑数量
 * @author chen
 * 修改于2013-10-22
 */
public class UpdateState extends MiddlewareActionService{
	//0代表粉丝数，1代表评论，2代表赞数，3代表通知,4代表在线答疑
	private String type;
	//用户id
	private String uid;
	/**
	 * 第一步：判断类型是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：更新数据库对应的数据
	 */
	public void execute(){
		if(DataUtils.checkString(type)){
			responseInfo("-1","类型不能为空");
			return;
		}
		int type1 = 0;
		try{
			type1 = Integer.parseInt(type);
		}catch(Exception e){
			responseInfo("-3","类型必须为整数");
			return;
		}
		String type2 = returnString(type1);
		if(DataUtils.checkString(type2)){
			responseInfo("-1","所填写的类型不存在");
			return;
		}
		String sql = "UPDATE wm_user_communityInfo SET "+type2 +" = 0 WHERE UID = ?";
		int count = middlewareService.update(sql,uid);
		if(count==0){
			responseInfo("-3","修改变动数状态失败");
			return;
		}
		responseInfo("1","修改变动状态成功");
	}
	
	/**
	 * 转化为数据库能修改的字段
	 * @return
	 */
	private String returnString(int type){
		String str = "";
		switch(type){
		case 0 : str = "FANSCHANGENUBER" ;break;
		case 1 : str = "COMMENTCHANGENUMBER" ;break;
		case 2 : str = "PRAISECHANGENUMBER" ;break;
		case 3 : str = "NOTICECHANGENUMBER" ;break;
		case 4 : str = "CIRCLENUMBER" ;break;
		default: str = "" ;
		}
		return str;
	}
	
	/**
	 * 获取通知变化数，赞数，评论数
	 */
	public void getUserMessage(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "SELECT COMMENTCHANGENUMBER,PRAISECHANGENUMBER,NOTICECHANGENUMBER,CIRCLENUMBER FROM wm_user_communityInfo  WHERE UID = ?";
		List<Map<String,Object>>list_value = middlewareService.find(sql,uid);
		responseInfo("1","成功",list_value);
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

}
