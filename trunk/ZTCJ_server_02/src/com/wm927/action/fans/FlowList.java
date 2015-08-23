package com.wm927.action.fans;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

public class FlowList extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(FlowList.class);
	private String page;
	private String number;
	private String uid;
	private String ouid;
	/**
	 * 索引分析师鲜花
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：判断用户是否为分析师
	 * 第三步：查询当前分析师的鲜花总数与对应的赠送鲜花的人
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		//判断用户是否为分析师
		String checkAna = "SELECT COUNT(*) FROM wm_user_info WHERE UID=? AND ROLETAG != 2";
		Long anaCount = middlewareService.findCount(checkAna,uid);
		if(anaCount==0){
			responseInfo("-3","用户不是分析师或机构");
			return;
		}
		String sql = "SELECT finfo.UID,uinfo.PHOTO FROM wm_flow_info finfo " +
				"LEFT JOIN wm_user_info uinfo ON finfo.UID=uinfo.UID WHERE finfo.ANAID = ?";
		String countflow = "SELECT FLOWCOUNT FROM wm_user_communityInfo WHERE UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		String count = middlewareService.findBy(countflow,"FLOWCOUNT",uid);
		Map<String,Object> map_value = new HashMap<String,Object>();
		List<Object> list = new ArrayList<Object>();
		map_value.put("data", list_value);
		map_value.put("totalCount",  count);
		list.add(map_value);
		responseInfo("1","成功",list);
	}
	
	/**
	 * 赠送鲜花给分析师
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：验证用户是否已经赠送鲜花给分析师
	 * 第四步：进行入库操作(已经赠送则将鲜花数自加1，否则插入一条新的数据)
	 * @throws DateParseException 
	 */
	public void addFlow() {
		if(DataUtils.checkString(ouid)){
			responseInfo("-1","分析师ID不能为空");
			return;
		}
		if(DataUtils.checkString(uid)){
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(uid.equals( ouid ) ){
			responseInfo("-3","自己不能赠送鲜花给自己");
			return;
		}
		if(!checkUser(uid))
			return;
		//判断用户是否为分析师
		String checkAna = "SELECT COUNT(*) FROM wm_user_info WHERE UID=? AND ROLETAG !=2";
		Long anaCount = middlewareService.findCount(checkAna,ouid);
		if(anaCount==0){
			responseInfo("-3","赠送的用户不是分析师或机构");
			return;
		}
		//验证是否已经赠送过鲜花,并且一天只能赠送一次鲜花给分析师
		String checksql = "SELECT ADDDATE FROM wm_flow_info WHERE UID = ? AND ANAID = ?";
		String adddate = middlewareService.findBy(checksql,"ADDDATE",uid,ouid);
		String sql = "";
		if(DataUtils.checkString(adddate)){
			//为空代表第一次赠送鲜花
			sql = "INSERT INTO wm_flow_info (UID,ANAID,FLOWCOUNT)VALUES(?,?,?)";
			middlewareService.update(sql,uid,ouid,1);
		}else{
			//判断日期是否当天，当天则不能再赠送鲜花
			Calendar calendar = Calendar.getInstance();
			try {
				calendar.setTime(DateUtils.parseDate(adddate));
			} catch (Exception e) {
				responseInfo("-3","系统内部出错");
				logger.info("获取的日期格式不对 ----"+this.getClass().getSimpleName()+e.getMessage());
				return;
			}
			int sqlDate = calendar.get(Calendar.DAY_OF_YEAR);
			calendar.setTime(new Date());
			int newDate = calendar.get(Calendar.DAY_OF_YEAR);
			if(sqlDate == newDate){
				responseInfo("-3","一天只能赠送一次鲜花");
				return;
			}
			sql = "update wm_flow_info set FLOWCOUNT=FLOWCOUNT+1,ADDDATE = ? WHERE UID=? AND ANAID=?";
			middlewareService.update(sql,DateUtils.getCurrentTime(),uid,ouid);
		}
		//更新分析师鲜花总数
		String updateFlowCountSql = "UPDATE wm_user_communityInfo SET FLOWCOUNT = FLOWCOUNT+1 WHERE UID = ?";
		middlewareService.update(updateFlowCountSql,ouid);
		responseInfo("1","鲜花赠送成功");
	}
	/**
	 * 鲜花榜
	 */
	public void topFlow(){
		String sql = "SELECT com.FLOWCOUNT,com.UID FROM wm_user_roleAnalyst role LEFT JOIN wm_user_communityInfo com ON com.UID=role.UID WHERE role.AUDITSTATE=1 ORDER BY com.FLOWCOUNT DESC , com.UID DESC ";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_value = null;
		
		for(Map<String,Object> map : list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
		}
		Map<String,Object> pageMap = findPageSize(page, number, sql); 
		responseInfo("1","成功",list_value,pageMap);
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

	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}
	
	
}
