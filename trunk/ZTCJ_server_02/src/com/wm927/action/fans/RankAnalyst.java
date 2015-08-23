package com.wm927.action.fans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.commons.Decoder;
import com.wm927.service.impl.MiddlewareActionService;

public class RankAnalyst extends MiddlewareActionService{
	private String page;
	private String number;
	private String uid;
	//分析师昵称或者用户名
	private String name;
	//0按UID索引分析师，1按用户昵称索引分析师
	private String type;
	private String sort;
	private String sorttype;
	/**
	 * 分析师排行榜
	 */
	public void execute(){
		String sql = "SELECT role.UID  FROM wm_user_roleAnalyst role LEFT JOIN  wm_user_communityInfo com ON com.UID=role.UID WHERE role.AUDITSTATE = 1  ORDER BY  com.BROWSENUMBER DESC ";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_value = null;
		
		for(Map<String,Object> map : list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
		}
		Map<String,Object> pageMap = findPageSize(page, number, sql); 
		responseInfo("1","成功",list_value,pageMap);
	}
	/**
	 * 索引分析师
	 */
	public void findAnalyst(){
		if(!checkNull(new Object[]{"索引类型不能为空"},new Object[]{type}))
			return ; 
		String sql = "SELECT UID,SUMMY,PHOTO,NICKNAME,REALNAME,USERNAME FROM wm_user_info WHERE UID = ?";
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		if("0".equals(type)){
			if(DataUtils.checkString(uid)){
				responseInfo("-1","用户ID不能为空");
				return;
			}
			//uid索引分析师
			if(!checkAnalystRole(uid))
				return;
			list_value = middlewareService.find(sql,uid);
		}else{
			if(DataUtils.checkString(name)){
				responseInfo("-1","索引名称不能为空");
				return;
			}
			//用户名或者昵称索引分析师
			String findUid = "SELECT UID FROM wm_user_info  "+
							"	WHERE (NICKNAME LIKE '%"+name+"%' OR USERNAME LIKE '%"+name+"%'  OR REALNAME LIKE '%"+name+"%')"+
							"	AND (ROLETAG = 3 OR ROLETAG = 5) ";
			List<Map<String,Object>> list_uid  = middlewareService.find(findUid);
			if(list_uid == null || list_uid.isEmpty()){
				responseInfo("1","无记录");
				return;
			}
			Map<String,Object> map_value = null;
			for(Map<String,Object> map : list_uid){
				map_value = new HashMap<String,Object>();
				map_value  = middlewareService.findFirst(sql,map.get("uid"));
				list_value.add(map_value);
			}
		}
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 分析师房间，显示分析师排行，是否在线等信息
	 */
	public void analystRoom(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		String sql = "SELECT rank.total_profit AS SUMRETURN ,rank.month_profit AS MONTHLYRETURN ,rank.odds AS ODDS,rank.count_order AS HDSUM, "+ 
					"	rank.avg_profit AS AVGRETURN, rank.max_profit AS MAXPROFIT ,rank.daliy_order AS DAYAVGHDSUM, "+
					"	rank.max_open_cycle AS MAXMONTHDAY, rank.min_open_cycle AS MINMONTHDAY ,"+
					"   urole.UID,ucom.BROWSENUMBER ,ucom.FLOWCOUNT, "+
					"   ucom.FANSSUM,ucom.ATTENTIONSUM,ucom.BLOGSUM,ucom.FANSCHANGENUBER,ucom.COMMENTCHANGENUMBER,ucom.PRAISECHANGENUMBER,ucom.NOTICECHANGENUMBER "+
					"	FROM wm_user_roleAnalyst urole LEFT JOIN wm_user_rank rank ON rank.uid=urole.UID AND rank.type=0" +
					"   LEFT JOIN  wm_user_communityInfo ucom ON ucom.UID = urole.UID WHERE urole.AUDITSTATE = 1 ";
		if("1".equals(type)){
			//显示自己关注的
			sql = "SELECT rank.total_profit AS SUMRETURN ,rank.month_profit AS MONTHLYRETURN ,rank.odds AS ODDS,rank.count_order AS HDSUM,"+ 
				"	rank.avg_profit AS AVGRETURN, rank.max_profit AS MAXPROFIT ,rank.daliy_order AS DAYAVGHDSUM, 	rank.max_open_cycle AS MAXMONTHDAY,"+
				"	rank.min_open_cycle AS MINMONTHDAY ,   urole.UID,ucom.BROWSENUMBER ,ucom.FLOWCOUNT, "+
				"	ucom.FANSSUM,ucom.ATTENTIONSUM,ucom.BLOGSUM,ucom.FANSCHANGENUBER,ucom.COMMENTCHANGENUMBER,ucom.PRAISECHANGENUMBER,ucom.NOTICECHANGENUMBER "+
				"	FROM wm_user_roleAnalyst urole"+
				"	LEFT JOIN wm_user_rank rank ON rank.uid=urole.UID AND rank.type=0"+
				"	LEFT JOIN wm_user_friends ufr ON ufr.FID = urole.UID  "+
				"	LEFT JOIN  wm_user_communityInfo ucom ON ucom.UID = urole.UID"+
				"	WHERE urole.AUDITSTATE=1 AND ufr.UID= "+uid;
		}
		sql += orderSort(sort);
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_value = null;
		for(Map<String,Object> map : list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
			map.put("state", attentionState(uid, String.valueOf(map.get("uid"))));
		}
		Map<String,Object> pageMap = findPageSize(page, number, sql); 
		responseInfo("1","成功",list_value,pageMap);
	}
	private String orderSort (String sort){
		int praseSort = DataUtils.praseNumber(sort);
		String orderSort = "";
		String sortType = " DESC";
		if("1".equals(sorttype)){
			sortType = " ASC";
		}
		switch(praseSort){
		case 0 : orderSort = " ORDER BY ucom.BROWSENUMBER " +sortType;
			break;
		case 1 : orderSort = " ORDER BY ucom.FANSSUM " +sortType; 
			break;
		case 2 : orderSort = " ORDER BY rank.total_profit "+sortType;
			break;
		default  : orderSort = " ORDER BY ucom.BROWSENUMBER "+sortType;
			break;
		}
		return orderSort;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(!DataUtils.checkString(name))
			name = Decoder.decode(name);
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public String getSorttype() {
		return sorttype;
	}
	public void setSorttype(String sorttype) {
		this.sorttype = sorttype;
	}
	
}
