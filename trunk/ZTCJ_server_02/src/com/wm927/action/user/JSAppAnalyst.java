package com.wm927.action.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.MD5Utils;
import com.wm927.commons.PageUtils;
import com.wm927.service.impl.MiddlewareActionService;

public class JSAppAnalyst extends MiddlewareActionService{
	private String uid ;//用户id
	private String ouid  ;//分析师id
	private String page;
	private String number ;
	private String lastid;
	private String purview;
	private String content;
	private String size;
	private String callback ;//跨域调用号
	private String type1;
	private String type2;
	private String type3;
	private String lastid1;
	private String lastid2;
	private String lastid3;
	
	/**
	 * 博客分类为开户指南的博客列表，做分页
	 */
	public void findAppBlogList(){
		if(!checkNull(new Object[]{"用户ID不能为空","跨域调用端口不能为空"},new Object[]{uid,callback},callback))
			return ; 
		if(!checkAppAnalystRole(uid, callback))
			return;
		String sql = "SELECT ID,BLOGTITLE,IMGLIST,ISPUBLIC,CLASSID, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',COMMENTNUMBER,BROWSENUMBER,BLOGSUMMY FROM wm_blog_info WHERE UID = "+uid+" AND ISPUBLIC=1 AND ISDELETE = 0 AND CLASSID=7 ";
		if(!DataUtils.checkString(lastid)){
			//动态加载更多
			sql += " AND ID < "+lastid;
		}
		sql += " ORDER BY ID DESC LIMIT "+ PageUtils.getPageCount(size);
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo( "1","成功",list_value, callback);
	}
	
	/**
	 * 返回黄金、白银、外汇、其它分类的文章，不带评论做分页
	 */
	public void findAppMoreBlogList(){
		if(!checkNull(new Object[]{"用户ID不能为空","跨域调用端口不能为空"},new Object[]{uid,callback},callback))
			return ; 
		String sql = "SELECT ID,BLOGTITLE,IMGLIST,ISPUBLIC,CLASSID, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',COMMENTNUMBER,BROWSENUMBER,BLOGSUMMY FROM wm_blog_info WHERE UID = "+uid+" AND ISPUBLIC=1 AND ISDELETE = 0 AND (CLASSID=1 OR CLASSID=2 OR CLASSID=3 OR CLASSID=4) ";
		if(!DataUtils.checkString(lastid)){
			//动态加载更多
			sql += " AND ID < "+lastid;
		}
		sql += " ORDER BY `ADDTIME` DESC LIMIT "+ PageUtils.getPageCount(size);
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo("1","成功", list_value,callback);
	}
	
	/**
	 * 返回当前分析师机构下，机构发表的三篇最新文章，若没有三篇文章则可以减少，若都没有则返回空
	 */
	public void getOrgBlog(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid},callback))
			return ; 
		String sql = "SELECT ID,BLOGTITLE,IMGLIST,CLASSID,KEYWORD,PRAISENUMBER,COMMENTNUMBER,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',BROWSENUMBER FROM wm_blog_info WHERE UID = ? ORDER BY ID DESC LIMIT 3";
		String orgidSql = "SELECT OID FROM wm_user_roleAnalyst WHERE UID = ?";
		String orgid = middlewareService.findBy(orgidSql, "OID",uid);
		List<Map<String,Object>> list_value = middlewareService.find(sql,orgid);
		responseInfo("1","成功",list_value,callback);
	}
	
	/**
	 * JS登录
	 */
	public void callbacklogin(){
		String username = getParameter("username");
		String password = getParameter("password");
		if(DataUtils.checkString(username)){
			responseInfo("-1","用户名不能为空",callback);
			return ;
		}
		if(DataUtils.checkString(password)){
			responseInfo("-1","密码不能为空",callback);
			return ;
		}
		String usernameLogin = "SELECT ID,PASSWORD FROM wm_user_index WHERE USERNAME = ?";
		Map<String,Object> map = middlewareService.findFirst(usernameLogin,username);
		if(map==null||map.isEmpty()){
			responseInfo("-1","输入的账号不存在",callback);
			return ;
		}
		if(DataUtils.checkString(map.get("password"))||DataUtils.checkString(password)){
			responseInfo("-1","输入的密码错误",callback);
			return ;
		}
		if(!MD5Utils.encrypt(password).equals(map.get("password"))){
			responseInfo("-1","输入的密码错误",callback);
			return ;
		}
		String ipAddress = getIpAddr();
		String updateSql = "UPDATE wm_user_index SET LOGINCOUNT=LOGINCOUNT+1 ,LASTLOGINTIME = ?,LASTLOGINIP = ?,LASTUPDATETIME = ?,LASTUPDATEIP = ? where ID = ? ";
		
		middlewareService.update(updateSql,DateUtils.getCurrentTime(),ipAddress,DateUtils.getCurrentTime(),ipAddress,map.get("id"));
		String updateOnline = "UPDATE wm_user_info SET ISONLINE = 1 WHERE UID=?";
		middlewareService.update(updateOnline,uid);
		//登录成功，返回登录个人信息
		String indexSql = "SELECT uindex.ID AS UID,uindex.STEP,uindex.TELENO,uindex.LOGINCOUNT,uindex.EMAIL,uindex.USERNAME,uindex.ISLOCK FROM wm_user_index uindex WHERE uindex.ID = ?";
		String roletag = "SELECT ROLETAG FROM wm_user_info WHERE UID = ?";
		
		Map<String,Object> map_value = middlewareService.findFirst(indexSql,map.get("id"));
		map_value.put("roletag", middlewareService.findBy(roletag, "ROLETAG",map.get("id")));
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		list_value.add(map_value);
		responseInfo("1","返回成功",list_value,callback);
	}
	
	
	/**
	 * 返回APP首页信息博客为开户指南的最新博客数量
	 */
	public  void getAppState(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空",callback);
			return;
		}
		
		if(DataUtils.checkString(callback)){
			responseInfo("-1","跨域调用端口不能为空",callback);
			return;
		}
		String number = "0";
		if(DataUtils.checkString(type1)){
			responseInfo("-1","类型1不能为空",callback);
			return;
		}
		if(DataUtils.checkString(type2)){
			responseInfo("-1","类型2不能为空",callback);
			return;
		}
		if(DataUtils.checkString(type3)){
			responseInfo("-1","类型3不能为空",callback);
			return;
		}
		if(!"0".equals(type1)){
			String sql = "SELECT COUNT(ID) AS NUMBER FROM wm_blog_info WHERE UID = "+uid+" AND ISPUBLIC=1 AND ISDELETE = 0 AND CLASSID=7 AND ID > " +lastid1;
			number = middlewareService.findBy(sql,"NUMBER");
		}else{
			String sql = "SELECT COUNT(ID) AS NUMBER FROM wm_blog_info WHERE UID = "+uid+" AND ISPUBLIC=1 AND ISDELETE = 0 AND CLASSID=7 AND  ADDTIME BETWEEN '" +DateUtils.getCurrentDate() +" 00:00:01 '" +" AND '"+DateUtils.getCurrentDate()+" 23:59:59 '";
			number = middlewareService.findBy(sql,"NUMBER");
		}
		
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("number1", number);map_value.put("number2", getAppMoreState());map_value.put("number3", getAppHdState());
		list_value.add(map_value);
		responseInfo("1","成功", list_value,callback);
		
	}
	
	/**
	 * 返回APP首页信息博客为黄金，白银，外汇，其它的最新博客数量
	 */
	public  String getAppMoreState(){
		String sql = "SELECT COUNT(ID) AS NUMBER FROM wm_blog_info WHERE UID = "+uid+" AND ISPUBLIC=1 AND ISDELETE = 0 AND (CLASSID=1 OR CLASSID=2 OR CLASSID=3 OR CLASSID=4) AND ID > " +lastid2;
		if("0".equals(type2)){
		}
		
		return middlewareService.findBy(sql,"NUMBER");
		
	}
	
	/**
	 * 返回实战圈子，喊单的最新数据更新的数目
	 */
	public  String getAppHdState(){
		String sql = "SELECT COUNT(*) AS NUMBER FROM wm_user_dynamic dy "+
				"	WHERE dy.UID = "+uid+"  AND dy.ID > " +lastid3;
		if("0".equals(type3)){
			   sql = "SELECT COUNT(*) AS NUMBER FROM wm_user_dynamic dy "+
					"	WHERE dy.UID = "+uid+" AND  dy.ADDTIME BETWEEN '" +DateUtils.getCurrentDate() +" 00:00:00 '" +" AND '"+DateUtils.getCurrentDate()+" 23:59:59 '";
		}
		
		return middlewareService.findBy(sql,"NUMBER");
		
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
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getLastid() {
		return lastid;
	}
	public void setLastid(String lastid) {
		this.lastid = lastid;
	}
	public String getPurview() {
		return purview;
	}
	public void setPurview(String purview) {
		this.purview = purview;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getType1() {
		return type1;
	}
	public void setType1(String type1) {
		this.type1 = type1;
	}
	public String getType2() {
		return type2;
	}
	public void setType2(String type2) {
		this.type2 = type2;
	}
	public String getType3() {
		return type3;
	}
	public void setType3(String type3) {
		this.type3 = type3;
	}
	public String getLastid1() {
		return lastid1;
	}
	public void setLastid1(String lastid1) {
		this.lastid1 = lastid1;
	}
	public String getLastid2() {
		return lastid2;
	}
	public void setLastid2(String lastid2) {
		this.lastid2 = lastid2;
	}
	public String getLastid3() {
		return lastid3;
	}
	public void setLastid3(String lastid3) {
		this.lastid3 = lastid3;
	}
	
	
}
