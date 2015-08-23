package com.wm927.action.attention;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

//显示经济数据，接口30320 定制经济数据
//http://192.168.0.53:11927/server/30320.action?uid=1772
//http://localhost:8080/Middleware_server/30320.action?uid=1772
public class ShowEcolomicAction   extends MiddlewareActionService {
	Logger log = Logger.getLogger(ShowEcolomicAction.class.getName());
	private  String uid;
	
	public void execute(){
			if(DataUtils.checkString(uid)){
				responseInfo("-1","用户ID为空");
				return;
			}
			if(!checkUser(uid))
				return;
			//社区数据拿国家
			String countryName = attentionCountry();
			String whereSql = "";
			if(!DataUtils.checkString(countryName)){
				whereSql = " AND  AREANAME IN ( " +countryName+ " )";
			}
				
			String nowdate = DateUtils.getCurrentDate();
			//mssql拿经济数据
			String sqlser = "SELECT areaname,newdate,newtime,indexyear,indextime,indexname,economicdata," +
					" prevalue,nextvalue FROM wm_data_economic " +
					" WHERE LENGTH(ECONOMICDATA)>0 AND ISPUBLIC = 1  " +whereSql +
					" AND NEWDATE <='"+nowdate+"' ORDER BY NEWDATE DESC ,NEWTIME DESC LIMIT 10 ";
			
			List<Map<String, Object>> list_value = middlewareService.find(sqlser);
			if(list_value == null || list_value.size() ==0){
				responseInfo("1", "该用户没有记录");
				return;
			}
			String newtime = "";
			for(Map<String, Object> map:list_value){
				newtime = map.get("newdate")+" "+map.get("newtime");
				map.put("createtime", newtime);
			}
			responseInfo("1","资讯记录",list_value);
	}
	
	/**
	 * 关注国家
	 * @return
	 */
	private String attentionCountry (){
		//关注的地区
		String attentionCountry = "SELECT  CID  FROM wm_user_attentionCountry WHERE UID = ?";
		List<Map<String,Object>> attentionCountry_list = middlewareService.find(attentionCountry,uid);
		if(attentionCountry_list == null || attentionCountry_list.isEmpty() ){
			return "";
		}
		String countryName = "SELECT AREANAME FROM wm_setting_area  WHERE ID IN ( ";
		StringBuilder con_sb = new StringBuilder();
		for(Map<String, Object> map :attentionCountry_list){
			con_sb.append(",");
			con_sb.append(" ' ");
			con_sb.append(map.get("cid"));
			con_sb.append(" ' ");
		}
		if(con_sb != null && con_sb.length() > 0){
			con_sb.append(" ) ");
			countryName += con_sb.substring(1);
		}
		List<Map<String,Object>> list_value = middlewareService.find(countryName);
		StringBuilder allsb = new StringBuilder();
		for(Map<String,Object> map:list_value){
			allsb.append(", '" + map.get("areaname") + "'");
		}
		if(allsb != null && allsb.length() > 0){
			return allsb.substring(1);
		}
		return "";
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
