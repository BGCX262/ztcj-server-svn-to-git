package com.wm927.action.data;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 赚到金币与我的购买接口
 * @author chen
 *
 */
public class AccountList extends MiddlewareActionService{
	private String uid;
	private String ouid;
	private String type="6";//购买分类1好评，2礼物，3秘笈，4服务，5策略服务  ,6全部  默认全部
	private String transtype ;//0代表一天，1代表一周，2代表一月，3代表三个月，4代表一年，5代表全部，默认5
	private String page;
	private String number;
	
	/**
	 * 赚到金币
	 * 
	 * @throws ParseException 
	 */
	public void execute() {
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return;
		int dtype = DataUtils.praseNumber(type, 6);	
		String dateStr = "";
		String dtranstype = DateUtils.praseBeginDate(transtype);
		if( !DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		String typeStr = "";
		if(dtype != 6){
			typeStr = " AND TYPE = " + dtype;
		}
		
		String sql = "SELECT ID,GIFTID,TYPE,SENDID,NUMBER,PRICE,MONEY,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'" +
				"     FROM wm_user_gift_detail  WHERE UID = "+uid + typeStr+dateStr+" ORDER BY ID DESC ";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> user_info = null;
		for(Map<String,Object> map : list_value){
			user_info = findUserInfo(map.get("sendid"));
			map.put("sendusername", user_info.get("username"));
			map.put("sendnickname", user_info.get("nickname"));
			map.putAll(giftName(map.get("type"),map.get("giftid")));
		}
		//累积进账，具有筛选条件的
		String allMoneySql = "SELECT  SUM(MONEY) AS ALLMONEY FROM wm_user_gift_detail   WHERE UID = "+uid + typeStr+dateStr;
		//累积总账，一共赚取多少金币
		String totalMoneySql = "SELECT  SUM(MONEY) AS TOTALMONEY FROM wm_user_gift_detail  WHERE UID = "+uid;
		String totalMoney = middlewareService.findBy(totalMoneySql, "TOTALMONEY");
		String allMoney = middlewareService.findBy(allMoneySql, "ALLMONEY");
		List<Object> list = new ArrayList<Object>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("totalmoney", totalMoney);
		map_value.put("allmoney", allMoney);
		map_value.put("data", list_value);
		list.add(map_value);
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list,mapInfo);
	}
	
	/**
	 * 我的购买
	 * @throws ParseException 
	 */
	public void consume() throws ParseException{
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的分析师ID不能为空"},new Object[]{uid,ouid}))
			return;
		
		int dtype = DataUtils.praseNumber(type, 6);	
		String dtranstype = DateUtils.praseBeginDate(transtype);
		String dateStr = "";
		if(!DataUtils.checkString(dtranstype)){
			dateStr = " AND ADDTIME BETWEEN '" + dtranstype + " 00:00:00' AND ";
			dateStr += "'"+DateUtils.getCurrentDate()+" 23:59:59'";
		}
		String typeStr = "";
		if(dtype != 6){
			typeStr = " AND TYPE = " + dtype;
		}
		//策略服务暂时不显示在我的购买里面
		String sql = "SELECT ID,UID,GIFTID,TYPE,SENDID,NUMBER,PRICE,MONEY,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME'" +
				"     FROM wm_user_gift_detail  WHERE  UID = "+ouid +" AND SENDID = "+uid+ typeStr+dateStr+" AND TYPE!=5 ORDER BY ID DESC ";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> userInfo = null;
		for(Map<String,Object> map : list_value){
			userInfo = findUserInfo(map.get("uid"));
			map.put("recphoto", userInfo.get("photo"));
			map.put("recnickname", userInfo.get("nickname"));
			map.put("recusername", userInfo.get("username"));
			userInfo = findUserInfo(map.get("sendid"));
			map.put("sendphoto", userInfo.get("photo"));
			map.put("sendnickname", userInfo.get("nickname"));
			map.put("sendusername", userInfo.get("username"));
			map.putAll(giftDetail(map.get("type"),map.get("giftid"))) ;
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	
	/**
	 * 礼物分为5中，所以需要根据TYPE去拿对应的具体礼物名称
	 * @param type
	 * @param id
	 * @return
	 */
	private Map<String,Object> giftName(Object type ,Object id){
		int ttype = DataUtils.praseNumber(type+"", 1);
		String sql = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift_type WHERE ID = ? ";//获取详细礼物
		String sql1 = "SELECT TITLE AS GIFTCONTENT  FROM wm_blog_view WHERE ID = ? ";//获取策略标题
		String sql2 = "SELECT GIFTIMG FROM wm_user_gift WHERE ID=? ";//获取礼物类型数据
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> map_value = middlewareService.findFirst(sql2,ttype);
		switch(ttype){
		case 2 : 
			map_value = middlewareService.findFirst(sql, id) ;
			break;
		case 3 : 
			map = middlewareService.findFirst(sql1, id) ;
			map_value.putAll(map);
			break;
		case 4 : 
			map = middlewareService.findFirst(sql1, id) ;
			map_value.putAll(map);
			break;
		default : 
			break;
		}
		return map_value;
		
	}
	
	/**
	 * 根据礼物的类型和ID去获取详细信息
	 * @param type
	 * @param id
	 * @return
	 */
	private Map<String,Object> giftDetail(Object type ,Object id){
		int ttype = DataUtils.praseNumber(type+"", 1);
		//礼物名称
		String sql = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift_type WHERE ID = ? ";
		String sql2 = "SELECT NAME AS GIFTCONTENT,GIFTIMG FROM wm_user_gift WHERE ID = ? ";
		//策略、秘笈
		String sql1 = "SELECT ID,CONTENT ,CLASSID, PURVIEWID , IMGLIST,VIDEOURI,VIDEOIMG,VIDEOTITLE ,VIDEOLINK,VIDEOHOSTS, TITLE ,ATTACHMENT,ATTACHNAME,ATTACHSIZE, PRICE ,EXPIRES,TIMELINESS,BUYNUMBER" +
						 " FROM wm_blog_view WHERE ID = ?";
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> map_value = middlewareService.findFirst(sql2,type);
		switch(ttype){
		case 2 : 
			map_value = middlewareService.findFirst(sql,id);
			break;
		case 3 : 
			map = middlewareService.findFirst(sql1,id) ; 
			map_value.putAll(map);
			break;
		case 4 : 
			map = middlewareService.findFirst(sql1,id) ; 
			map_value.putAll(map);
			break;
		default : 
			break;
		}
		return map_value;
		
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
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getTranstype() {
		return transtype;
	}
	public void setTranstype(String transtype) {
		this.transtype = transtype;
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
	
}
