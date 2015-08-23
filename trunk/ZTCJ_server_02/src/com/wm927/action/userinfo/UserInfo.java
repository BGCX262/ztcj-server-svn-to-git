package com.wm927.action.userinfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.commons.RoleUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 基本信息
 * @author chen
 * 修改于2013-10-22
 */
public class UserInfo extends MiddlewareActionService{
	//被查找人id
	private String uid;
	//查找人id
	private String ouid;
	
	/**
	 * 获取个人基本信息
	 */
	public void getUserInfo(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户名ID不能为空");
		}
		if(!checkUser(uid)){
			return;
		}
		String indexSql = " SELECT USERNAME,TELENO,EMAIL FROM wm_user_index WHERE ID = ?";
		//返回的是普通用户和分析师信息
		String infoSql = "SELECT info.ROLETAG,info.REALNAME,info.NICKNAME,info.BIRTHDAY,info.RECIEVEMSG," +
				"	info.BELONGS,info.INVESTMENT , info.GENDER,info.WORKYEAR,info.INTEREST,info.PHOTO," +
				"   info.PLACE,info.SUMMY,info.ADDTIME,info.JOB,info.TARDEMETHOD FROM wm_user_info info " +
				"   WHERE info.UID = ?" ;
		//0代表普通或者分析师，1代表机构
		boolean checkRole = false;
		String role = checkRole(uid);
		if(RoleUtils.ORGANIZATION_ROLE.equals(role) || RoleUtils.APPORGANIZATION_ROLE.equals(role)){
			checkRole = true;
		}
		if(checkRole){
			infoSql = "SELECT info.ROLETAG,info.PHOTO,info.REALNAME,info.NICKNAME," +
					"  info.RECIEVEMSG,info.BELONGS,info.PLACE,info.INVESTMENT,org.ORGANIZATIONNAME,org.CONTACT," +
					"  org.CREATETIME,org.WEB,org.APPLOGO,org.ABBREVIATION AS SUMMY,org.TELEPHONE,INTEREST " +
					"  FROM  wm_user_info info LEFT JOIN wm_user_roleOrganization org ON info.UID = org.UID" +
					"   WHERE info.UID = ?";
		}
		
		Map<String,Object> info_map = middlewareService.findFirst(infoSql,uid);
		Map<String,Object> index_map = middlewareService.findFirst(indexSql,uid);
		//拆分地区
		String place = String.valueOf(info_map.get("place"));
		String newphone = String.valueOf(index_map.get("teleno"));
		newphone = DataUtils.checkString(newphone)||newphone.length()<7 ?"":newphone.substring(0,3)+"****"+newphone.substring(7);
		info_map.put("teleno", newphone);
		info_map.put("type", checkRole ?"1":"0");
		String places[] = place.split(",");
		
		String araename = "";
		String areanames = "";
		String checkplace = "SELECT AREANAME FROM wm_setting_area where ID = ?";
		for(String p:places){
			araename = middlewareService.findBy(checkplace,"AREANAME", p);
			areanames +=araename+" ";
		}
		info_map.put("placename", areanames);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		index_map.putAll(info_map);
		list_value.add(index_map);
		responseInfo("1","获取个人信息成功",list_value);
	}
	
	/**
	 * 用户主页头部信息
	 */
	public void getUserHeadInfo(){
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户ID不能为空"},new Object[]{uid,ouid}))
		if(!checkUser(ouid)){
			return;
		}
		if(!uid.equals(ouid)){
			//人气值加1
			String updateBrowSql = "UPDATE wm_user_communityInfo SET BROWSENUMBER=BROWSENUMBER+1 WHERE UID=?";
			middlewareService.update(updateBrowSql,ouid);
		}
		String infoSql = "SELECT info.OPENCALL,info.ISONLINE,info.ANNOUNCEMENT,info.USERNAME,info.ROLETAG,info.INTEREST,info.PHOTO,info.NICKNAME,info.WORKYEAR," +
				"  info.JOB,info.SUMMY,app.TWODIMENSIONALCODE AS QRCODE FROM wm_user_info info " +
				"  LEFT JOIN wm_userApp_info app ON app.UID = info.UID " +
				"  WHERE info.UID = ? ";
		Map<String,Object> info_map =  middlewareService.findFirst(infoSql,ouid);
		Map<String,Object> incommu_map1 =  findUserComInfo(ouid);
		Map<String,Object> incommu_map2 =  findUserCallInfo(ouid);
		List<Map<String,Object>>  list_value = new ArrayList<Map<String,Object>>();
		info_map.putAll(info_map);info_map.putAll(incommu_map1);info_map.putAll(incommu_map2);
		info_map.put("attentionstate", attentionState(uid, ouid));
		list_value.add(info_map);
		responseInfo("1","获取个人头部信息成功",list_value);
			
}
	
	/**
	 * 顶部公共部分用户信息
	 */
	public void getUserTopInfo(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String indexSql = "SELECT STEP ,USERNAME FROM wm_user_index WHERE ID = ?";
		String infoSql = "SELECT ROLETAG,NICKNAME,PHOTO,UID FROM wm_user_info WHERE UID = ?";
		String commuSql = "SELECT FANSSUM,FANSCHANGENUBER FROM wm_user_communityInfo  WHERE UID = ?";
		Map<String,Object> index_map = middlewareService.findFirst(indexSql,uid);
		Map<String,Object> info_map = middlewareService.findFirst(infoSql,uid);
		Map<String,Object> commu_map = middlewareService.findFirst(commuSql,uid);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		index_map.putAll(info_map);index_map.putAll(commu_map);
		list_value.add(index_map);
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 获取视频id
	 */
	public void getVideoUrl(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		String sql = "SELECT VIDEOID FROM wm_user_info WHERE UID = ? ";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 头像划过返回用户关注状态信息
	 * //0代表我未关注你，你也未关注我--1代表我关注你，你未关注我--2代表我关注了你，你也关注了我--3代表我未关注你，你关注了我我
	 * //进行数据库的反向判断，如果我关注了你则数据库存在的条件是UID->FID----如果你关注了我则数据库存在条件是FID->UID
	 */
	public void attentionState(){
		if(DataUtils.checkString(uid)){
			responseInfo("1","自己ID不能为空");
			return;
		}
		if(DataUtils.checkString(ouid)){
			responseInfo("1","他人ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		if(!checkUser(ouid))
			return;
		String findUserInfo = "SELECT USERNAME,PHOTO,NICKNAME,SUMMY FROM wm_user_info  WHERE UID = ?";
		Map<String,Object> map_value = middlewareService.findFirst(findUserInfo,ouid);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		map_value.put("state", attentionState(uid,ouid));
	    list_value.add(map_value);
	    responseInfo("1","返回成功",list_value);
	}
	
	/**
	 * 获取角色对应的功能,ISOPEN代表是否开发，0代表隐藏，1代表开放
	 */
	public void findRoleFunction(){
		if(DataUtils.checkString(uid)){
			responseInfo("1","用户名不存在");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "SELECT ROLETAG FROM wm_user_info WHERE UID = ?";
		String uindexid = middlewareService.findBy(sql, "ROLETAG",uid);
		String findRoleListSql = "SELECT FUNCTIONID,ISOPEN FROM wm_role_function WHERE ROLEID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(findRoleListSql,uindexid);
		responseInfo("1","成功",list_value);
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
	
}
