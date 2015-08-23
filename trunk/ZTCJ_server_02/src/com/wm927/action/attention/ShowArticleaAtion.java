package com.wm927.action.attention;

import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * chen
 */
public class ShowArticleaAtion extends MiddlewareActionService {
	Logger log = Logger.getLogger(ShowArticleaAtion.class.getName());
	private  String page;
	private  String number;
	private  String uid;
	
	public void execute(){
				if(DataUtils.checkString(uid)){
					responseInfo("-1","用户ID不能为空");
					return;
				}
				if(!checkUser(uid))
					return;
				
				//动态切换至SqlServer数据库 
				String sqlall = "SELECT  ID,CHANNELID, DATE_FORMAT(PUBLISTTIME ,'%Y-%m-%d %H:%i:%s') AS 'PUBLISTTIME'  ,ARTICLETITLE FROM wm_article_info " +
						getArticleInfo()+ " ORDER BY ID DESC LIMIT 10";
				List<Map<String, Object>> list_value = middlewareService.find(sqlall);
				for(Map<String, Object> map : list_value){
					String channelid = (String)map.get("channelid");
					String id = (String)map.get("id");
					String getUrlByChannelId = ArticleRetuenUrl.GetUrlByChannelId(channelid,id );
					map.put("URL", getUrlByChannelId);
				}
				responseInfo("1", "资讯记录", list_value);//分页返回getpage 分页的信息(页面,每页条数,总条数,总页数)
	}
	private String getArticleInfo(){
		String sql = "";
		//拼接关注国家sql
		String countryName =  attentionCountry();
		if(DataUtils.checkString(countryName))
				return sql;
		List<Map<String,Object>> countryList = middlewareService.find(countryName);
		////拼接关注货币sql
		String currencyCode = attentionCurrency();
		if(DataUtils.checkString(currencyCode))
			return sql;
		List<Map<String,Object>> currencyList = middlewareService.find(currencyCode);
		
		StringBuilder allsb = new StringBuilder();
		for(Map<String,Object> map:countryList){
			allsb.append("OR ARTICLETITLE LIKE '%" + map.get("areaname") + "%' ");
		}
		for(Map<String,Object> map:currencyList){
			allsb.append("OR ARTICLETITLE LIKE '%" + map.get("areaname") + "%' ");
		}
		if(allsb.length() == 0){
			responseInfo("-1","没有相关数据");
			return sql;
		}
		sql = " WHERE " + allsb.substring(2);
		return sql;
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
		return countryName;
	}
	/**
	 * 关注货币
	 * @return
	 */
	private String attentionCurrency (){
		//关注货币
		String attentionCurrency = "SELECT  CID  FROM  wm_user_attentionCurrency WHERE UID = ?";
		List<Map<String,Object>> attentionCurrency_list = middlewareService.find(attentionCurrency,uid);
		if(attentionCurrency_list == null || attentionCurrency_list.isEmpty() ){
			return "";
		}
		String currencyCode = "SELECT NAME FROM wm_user_attentionCurrency  WHERE ID IN ( ";
		StringBuilder cur_sb = new StringBuilder();
		for(Map<String, Object> map :attentionCurrency_list){
			cur_sb.append(",");
			cur_sb.append(" ' ");
			cur_sb.append(map.get("cid"));
			cur_sb.append(" ' ");
		}
		if(cur_sb != null && cur_sb.length() > 0){
			cur_sb.append(" ) ");
			currencyCode += cur_sb.substring(1);
		}
		return currencyCode;
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



	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
