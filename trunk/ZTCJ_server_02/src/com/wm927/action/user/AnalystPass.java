package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wm927.commons.Contants;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 机构认证(通过，取消)
 */
public class AnalystPass extends MiddlewareActionService{
	private String pass;//审核通过0代表未通过，1代表通过
	private String uid;
	private String ouid ;//机构商ID
	private String aid;
	/**
	 * 机构认证(通过，取消)
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","通知ID不能为空","取消通过状态不能为空"},new Object[]{uid,aid,pass}))
			return ; 
		String checkAnaExcit = "";
		Long checkCount ;
		String msg = "";
		String time = DateUtils.getCurrentTime();
		//修改分析师认证
		String update = "UPDATE wm_user_roleAnalyst SET AUTHENTICATESTATE = ?,AUTHENTICATETIME = ? , OID = ? WHERE UID = ?";
		//发出一条通知给用户
		String advice = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP,TYPE)VALUES(?,?,?,?,?,?)";
		//发出一条通知，留给机构当做历史记录，消除机构认证记录
		String advice2 = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP,READSTATE,TYPE)VALUES(?,?,?,?,?,?,?)";
		//删除机构认证记录
		String updateAdvice = "UPDATE wm_user_advice SET DELETESTATE = 1 WHERE ID = ?";
		String usersql = "SELECT NICKNAME FROM wm_user_info WHERE UID = ?";
		String name = middlewareService.findBy(usersql, "NICKNAME",uid);
		//获取机构的全名
		String orgsql = "SELECT ORGANIZATIONNAME FROM wm_user_roleOrganization WHERE UID = ?";
		String orgname = middlewareService.findBy(orgsql, "ORGANIZATIONNAME",ouid);
		if( "0".equals(pass)){
			middlewareService.update(update,0,time,Contants.DEFAULT_OID,uid);
			middlewareService.update(advice,uid,"很遗憾，你申请的绑定"+orgname+"机构未通过!",time,ouid,getIpAddr(),1);
			middlewareService.update(advice2,ouid,"您在"+DateUtils.getCurrentTime()+"拒绝了"+name+"的分析师绑定申请",time,ouid,getIpAddr(),1,1);
			msg = "取消机构绑定成功";
		}else{
			//通过分析师认证
			checkAnaExcit = "SELECT COUNT(*) FROM wm_user_roleAnalyst WHERE UID = ? AND AUTHENTICATESTATE = 2";
			checkCount = middlewareService.findCount(checkAnaExcit,uid);
			if(checkCount == 0){
				responseInfo("-3","当前分析师没有申请记录");
				return;
			}
			middlewareService.update(update,1,time,ouid,uid);
			middlewareService.update(advice,uid,"恭喜你已通过"+orgname+"机构绑定认证!",time,ouid,getIpAddr(),1);
			middlewareService.update(advice2,ouid,"您在"+DateUtils.getCurrentTime()+"接受了"+name+"的分析师绑定认证申请",time,ouid,getIpAddr(),1,1);
			msg = "通过机构绑定成功";
		}
		//通知变动数+1
		String adviceChange = "UPDATE wm_user_communityInfo SET NOTICECHANGENUMBER = NOTICECHANGENUMBER +1 WHERE UID = ?";
		middlewareService.update(adviceChange,uid);
		middlewareService.update(updateAdvice,aid);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("name", name);
		map_value.put("addtime", time);list_value.add(map_value);
		responseInfo("1",msg,list_value);
	}

	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
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
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	
	
}
