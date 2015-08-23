package com.wm927.action.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 通知
 *	返回：通知内容，通知发布时间，阅读状态，通知id
 *  307203
 *http://192.168.0.53/server/307203.action?uid=32&pageindex=1&pagesize=2
 */
public class Advice extends MiddlewareActionService{
	private  String uid;
	private  String page;
	private  String number;
	private  String aid;//通知ID
	private String content;
	
	/**
	 * 通知列表
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		String sql = "SELECT ID,CONTENT,SENDID, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME',TYPE FROM wm_user_advice WHERE UID = "+ uid+" AND DELETESTATE=0   ORDER BY ID DESC";
		checkNewAdvice();
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		String findInfo = "SELECT PHOTO,NICKNAME,USERNAME FROM wm_user_info WHERE UID = ?";
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map:list_value){
			map_value = middlewareService.findFirst(findInfo,map.get("sendid"));
			map.putAll(map_value);
		}
		Map<String,Object> page_map = findPageSize(page, number, sql);
		responseInfo( "1", "返回成功", list_value,page_map);
	}
	/**
	 * 详细通知
	 */
	public void adviceDetail(){
		if(!checkNull(new Object[]{"用户ID不能为空","通知ID不能为空"},new Object[]{uid,aid}))
			return ; 
		if(!checkUser(uid))
			return;
		String sql = "SELECT SENDID,CONTENT, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',READSTATE,TYPE FROM wm_user_advice WHERE ID = ? AND UID = ?";
		Map<String,Object> map_value = middlewareService.findFirst(sql,aid,uid);
		if(map_value == null ||map_value.isEmpty()){
			responseInfo("-3","当前通知不存在");
			return ;
		}
		String update = "UPDATE wm_user_advice SET READSTATE = 1,READIP = ?,READTIME = ? WHERE ID = ?";
		middlewareService.update(update,getIpAddr(),DateUtils.getCurrentTime(),aid);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		list_value.add(map_value);
		responseInfo("1","返回成功",list_value);
	}
	/**
	 * 删除通知
	 */
	public void deleteAdvice(){
		if(!checkNull(new Object[]{"用户ID不能为空","通知ID不能为空"},new Object[]{uid,aid}))
			return ; 
		if(!checkUser(uid))
			return;
		String del = "UPDATE wm_user_advice SET DELETESTATE = 1 WHERE UID = ? AND ID = ?";
		int count = middlewareService.update(del,uid,aid);
		if(count == 0){
			responseInfo("-3","通知删除失败");
			return;
		}
		responseInfo("1","通知删除成功");
	}
	/**
	 * 1,检查是否有系统通知加入个人通知
	 * 2,在wm_user_communityInfo 表存在当前用户存在多少数目的系统通知数
	 * 3,在wm_sys_advice_number 存在当前系统的通知数目 
	 * 4,如果两个数目相等代表没有系统通知发送否则代表有新的系统通知发送
	 * 5,通过系统通知数目-用户存在的通知数目，获取最新添加的通知数目，然后插入用户通知表
	 * 6,再更新系统智通总数目
	 */
	private void checkNewAdvice (){
		String checkUserAdviceSql= "SELECT ADVICENUMBER FROM wm_user_communityInfo WHERE UID = ?";
		String checkSysAdviceSql = "SELECT ADVICENUMBER FROM wm_sys_advice_number WHERE ID = 1";
		String userNumber = middlewareService.findBy(checkUserAdviceSql, "ADVICENUMBER",uid);
		String sysNumber = middlewareService.findBy(checkSysAdviceSql, "ADVICENUMBER");
		if(userNumber.equals(sysNumber)){
			return;
		}
		int number = 0;
		try{
			number =  Integer.parseInt(sysNumber) - Integer.parseInt(userNumber); 
		}catch(Exception e){
			return;
		}
		
		number = number > 0 ? number : -(number);
		String findAdvice = "SELECT CONTENT,ADDTIME,ADDADMINID,ADMINIP FROM wm_sys_advice ORDER BY ID DESC LIMIT " +number;
		List<Map<String,Object>> list_value = middlewareService.find(findAdvice);
		if( list_value == null || list_value.isEmpty()){
			return;
		}
		//将系统通知依次更新到个人通知
		String insertSysSql = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,SENDID,SENDIP,TYPE)VALUES(?,?,?,?,?,?)";
		for(Map<String,Object> map : list_value){
			middlewareService.update(insertSysSql,uid,map.get("content"),map.get("addtime"),map.get("addadminid"),map.get("adminip"),0);
		}
		//更新当前用户的系统通知数目
		String updateCom = "UPDATE wm_user_communityInfo SET ADVICENUMBER = ?  WHERE UID = ?";
		middlewareService.update(updateCom,sysNumber,uid);
	}
	/**
	 * 发送系统通知
	 */
	public void sendSysAdvice(){
		String sql = "INSERT INTO wm_sys_advice (CONTENT,ADDTIME,ADDADMINID,ADMINIP,DELETESTATE)VALUES (?,?,?,?,?)";
		int count = middlewareService.update(sql,content,DateUtils.getCurrentTime(),uid,getIpAddr(),0);
		if(count == 0){
			responseInfo("-3","发送系统通知失败");
			return;
		}
		//更新系统通知总数目
		String updateAdviceTotal = "UPDATE wm_sys_advice_number SET ADVICENUMBER = ADVICENUMBER +1 WHERE ID = 1";
		middlewareService.update(updateAdviceTotal);
		responseInfo("1","发送系统通知成功");
	}
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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
	public String getAid() {
		return aid;
	}
	public void setAid(String aid) {
		this.aid = aid;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
}