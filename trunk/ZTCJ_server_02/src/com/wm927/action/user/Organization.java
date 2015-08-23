package com.wm927.action.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.Contants;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 郭瑜嘉
 * 2013/11/20
 */
public class Organization extends MiddlewareActionService{
	private String uid;
	private String ouid;
	private String page;
	private String number;
	private String logourl;//logo
	private String oid;//机构ID
	private String anaid;//分析师id
	private String state;//APP绑定状态(0代表绑定，1代表解绑定)
	/**
	 * 机构商列表
	 */
	public void execute(){
		String sql = "SELECT org.UID, org.ORGANIZATIONNAME "+
					"	,org.WEB ,org.ABBREVIATION ,org.CONTACT,org.TELEPHONE,org.APPLOGO FROM wm_user_roleOrganization org"+
					"	RIGHT JOIN wm_userApp_info app ON  app.UID = org.UID"+
					"	WHERE org.AUDITSTATE = 1";
		responseInfo(page,number,"1","返回成功",sql);
	}
	/**
	 * 上传机构商图片
	 */
	public void uploadOrgLogo(){
		if(DataUtils.checkString(logourl)){
			responseInfo("-1","机构图片地址不能为空");
			return;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","机构ID不能为空");
			return;
		}
		if(!checkOrgRole(uid))
			return;
		String upload = "UPDATE wm_user_roleOrganization SET APPLOGO = ? WHERE UID = ?";
		int count  = middlewareService.update(upload,logourl,uid);
		if(count == 0){
			responseInfo("-3","机构商图片上传失败");
			return;
		}
		responseInfo("1","机构商图片上传成功");
	}
	/**
	 * 显示当前机构商下所属的分析师
	 */
	public void belongOrgAnalyst(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","机构ID不能为空");
			return;
		}
		if(!checkOrgRole(uid))
			return;
			
		String sql = "SELECT UID FROM wm_user_roleAnalyst WHERE OID = "+uid+" AND AUDITSTATE = 1";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		String userInfo = "SELECT NICKNAME,PHOTO,USERNAME FROM wm_user_info WHERE UID = ?";
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map: list_value){
			map_value = middlewareService.findFirst(userInfo,map.get("uid"));
			map.putAll(map_value);
		}
		Map<String,Object> pageMapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,pageMapInfo);
	}
	
	/**
	 * 检查分析师申请机构的状态
	 */
	public void checkAnalystRegistState(){
		String sql = "SELECT AUTHENTICATESTATE,AUTHENTICATETIME,AUTHENTICATEEXPLANATION FROM wm_user_roleAnalyst WHERE UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		responseInfo("1","成功",list_value);
	}
	
	
	/**
	 * 获取当前分析师绑定的机构
	 */
	public void findOrgForAnalyst(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","分析师ID不能为空");
			return;
		}
		String sql = "SELECT ana.OID,role.ORGANIZATIONNAME,role.ABBREVIATION,role.APPLOGO FROM wm_user_roleOrganization role LEFT JOIN wm_user_roleAnalyst ana ON ana.OID=role.UID WHERE ana.UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		responseInfo("1","成功",list_value);
	}
	/**
	 * 机构解除绑定分析师
	 */
	public void chanelAnalyst(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","机构ID不能为空");
			return;
		}
		if(DataUtils.checkString(ouid)){
			responseInfo("-1","分析师ID不能为空");
			return;
		}
		if(!checkOrgRole(uid))
			return;
		if(!checkAnalystRole(ouid))
			return;
		String checkAnalyst = "SELECT COUNT(*) FROM wm_user_roleAnalyst WHERE UID = ? AND OID = ?";
		Long count = middlewareService.findCount(checkAnalyst,ouid,uid);
		if(count == 0){
			responseInfo("-1","当前用户未绑定所指定机构，请检查");
			return;
		}
		String update = "UPDATE wm_user_roleAnalyst SET OID = ? WHERE UID = ?";
		int updateCount = middlewareService.update(update,Contants.DEFAULT_OID,ouid);
		if(updateCount == 0){
			responseInfo("-1","解绑分析师失败");
			return;
		}
		String cur_time = DateUtils.getCurrentTime();
		String orgnamesql = "SELECT ORGANIZATIONNAME  FROM wm_user_roleOrganization WHERE UID = ? ";
		String ananamesql = "SELECT NICKNAME  FROM wm_user_info  WHERE UID = ? ";
		String orgname = middlewareService.findBy(orgnamesql, "ORGANIZATIONNAME",uid);
		String ananame = middlewareService.findBy(ananamesql, "NICKNAME",ouid);
		//发出通知给当前用户
		String advice = "INSERT INTO wm_user_advice (UID,CONTENT,ADDTIME,READSTATE,SENDID,SENDIP,TYPE)VALUES(?,?,?,?,?,?,?)";
		middlewareService.update(advice,ouid,orgname + "已经与您解除了机构绑定关系",cur_time,0,uid,getIpAddr(),1);
		//发送通知给机构
		middlewareService.update(advice,uid,"您已经解除了"+ananame+"的绑定关系",cur_time,1,uid,getIpAddr(),1);
		responseInfo("1","解除绑定成功");
	}
	/**
	 * 获取机构分析师LOGO
	 * ① 根据uid去wm_user_roleAnayst拿oid
	 * ② 根据oid=wm_user_roleOrganization.UID表拿APPLOGO
	 * 郭瑜嘉
	 * 2013/11/20
	 */
	public void getLogo(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户id不能为空");
			return;
		}
		String sql = "SELECT OID FROM wm_user_roleAnalyst WHERE UID = ?";
		String oid = middlewareService.findBy(sql,"OID",uid);
		if(DataUtils.checkString(oid)){
			responseInfo("-1","当前分析师未绑定机构");
			return;
		}
		String appLogoSql = "SELECT APPLOGO FROM wm_user_roleOrganization WHERE UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(appLogoSql,oid);
		responseInfo("1","成功",list_value);
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
	public String getLogourl() {
		return logourl;
	}
	public void setLogourl(String logourl) {
		this.logourl = logourl;
	}
	
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public String getAnaid() {
		return anaid;
	}
	public void setAnaid(String anaid) {
		this.anaid = anaid;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getOuid() {
		return ouid;
	}
	public void setOuid(String ouid) {
		this.ouid = ouid;
	}
	
}
