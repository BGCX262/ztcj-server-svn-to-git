package com.wm927.action.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.ServerDomainUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 定制
 * @author chen
 * 分析师偏好接口
 * 修改于2013-10-22 
 */
public class Analyst extends MiddlewareActionService{
	//用户ID
	private String uid;
	//分析师ID
	private String ouid;
	//国家代码值，匹配国家中文表示
	private String countryCode;
	//货币代码值，匹配货币中文表示
	private String currencyCode;
	//表示删除还是新增(0删除，1新增)
	private String type;
	private String page;
	private String number;
	private static final Logger logger = Logger.getLogger(Analyst.class);
	/**
	 * 显示分析师接口
	 * 第一步：判断用户id是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：显示根据排序规则的前6个分析师
	 */
	public void execute(){
		//查询数据库推荐的关注分析师，按照分析师推荐表排序
		String sql = "SELECT UID FROM wm_user_roleAnalyst WHERE AUDITSTATE=1 AND ISTOATTENTION=1 ORDER BY SORTVALUE ";
		
		//手机端不需要UID判断状态
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		Map<String,Object> map_value = null;
		for(Map<String,Object> map:list_value){
			 //显示分析师主页地址
			map.put("anauri", ServerDomainUtils.ANALYST_URI+map.get("uid"));
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
			map.put("state", attentionState(uid, String.valueOf(map.get("uid"))));
		}
		responseInfo("1","返回成功",list_value);
		
	}
	/**
	 * 显示所有分析师(分页)
	 */
	public void showAllAnalyst(){
		String sql = "SELECT UID FROM wm_user_roleAnalyst WHERE AUDITSTATE=1 ORDER BY SORTVALUE,ID DESC";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_value = null;
		for(Map<String,Object> map:list_value){
			 //显示分析师主页地址
			map.put("anauri", ServerDomainUtils.ANALYST_URI+map.get("uid"));
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
		}
		Map<String,Object> pageMap = findPageSize(page, number, sql); 
		responseInfo("1","成功",list_value,pageMap);
	}
	/**
	 * 添加关注分析师
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户和分析师是否存在
	 * 第三步：根据类型来判断是关注分析师还是取消关注分析师
	 * 第四步：返回当前用户与分析师的关注状态
	 */
	public void addAnalyst(){
		if(uid.equals(ouid)){
			responseInfo("-3","自己不能关注自己");
			return;
		}
		
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户id为空","类型不能为空"},new Object[]{uid,ouid,type}))
			return ; 
		
		if(!checkUser(ouid)||!checkUser(uid))
			return;
		String msg = "关注成功";	
		String checkAttentionSql = "SELECT COUNT(*) FROM wm_user_friends WHERE UID = ? AND FID = ?";
		if("1".equals(type)){
			//新增关注分析师
			Long checkAnaExist = middlewareService.findCount(checkAttentionSql,uid,ouid);
			if(checkAnaExist > 0){
				responseInfo("-3","好友已经关注");
				return;
			}
			String insertsql = "INSERT INTO wm_user_friends (UID,FID,ATTENTIONTIME,TYPE)VALUES(?,?,?,?)";
			middlewareService.update(insertsql,uid,ouid,DateUtils.getCurrentTime(),0);
			//更新被关注用户的粉丝总数和粉丝变化数
			String updatestate = "UPDATE wm_user_communityInfo SET FANSCHANGENUBER = FANSCHANGENUBER+1,FANSSUM = FANSSUM+1 WHERE UID = ?";
			int statecount = middlewareService.update(updatestate,ouid);
			if(statecount==0){
				logger.info("更新关注状态失败"+this.getClass().getSimpleName());
			}
			//用户的关注数+1
			String updateFansChange = "UPDATE wm_user_communityInfo SET ATTENTIONSUM = ATTENTIONSUM+1 WHERE UID = ?";
			int fanschangecount = middlewareService.update(updateFansChange,uid);
			if(fanschangecount == 0){
				logger.info("更新用户的关注总数失败"+this.getClass().getSimpleName());
			}
		}else{
			//取消关注
			String deletesql = "DELETE FROM wm_user_friends WHERE UID = ? AND FID = ?";
			int delCount = middlewareService.update(deletesql,uid,ouid);
			//因为网络可能延迟，导致其实已经取消关注，但是前端并未有返回状态发送，所以后面的操作不必执行
			if(delCount == 0){
				responseInfo("1","取消关注成功");
				return;
			}
			//更新被关注用户的粉丝总数和粉丝变化数
			String updatestate = "UPDATE wm_user_communityInfo SET FANSCHANGENUBER = FANSCHANGENUBER-1,FANSSUM = FANSSUM-1 WHERE UID = ?";
			int statecount = middlewareService.update(updatestate,ouid);
			if(statecount==0){
				responseInfo("-3"," 更新关注状态失败");
				return;
			}
			//用户的关注数-1
			String updateFansChange = "UPDATE wm_user_communityInfo SET ATTENTIONSUM = ATTENTIONSUM-1 WHERE UID = ?";
			int fanschangecount = middlewareService.update(updateFansChange,uid);
			if(fanschangecount == 0){
				logger.info("更新用户的关注总数失败"+this.getClass().getSimpleName());
			}
			msg = "取消关注成功";
		}
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		Map<String,Object> map_value = new HashMap<String,Object>();
		map_value.put("state", attentionState(uid, ouid));
		list_value.add(map_value);
		responseInfo("1",msg,list_value);
	}
	
	/**
	 * 批量关注分析师
	 * 未修改关注数和粉丝数，需要修改
	 */
	public void addMoreAnalyst(){
		if(!checkNull(new Object[]{"用户ID不能为空","被索引的用户id为空"},new Object[]{uid,ouid}))
			return ; 
		if(!checkUser(uid))
			return;
		List<String> anaids = Arrays.asList(ouid.split(","));
		String checkAttentionSql = "SELECT COUNT(*) FROM wm_user_friends WHERE UID=? AND FID = ?";
		String insertAttentionSql = "INSERT INTO wm_user_friends (UID,FID,ATTENTIONTIME)VALUES(?,?,?)";
		String checkAna = "SELECT ID FROM wm_user_index where ID = ?";
		String date = DateUtils.getCurrentTime();
		Long count ;
		String anaid = null;
		for(String s:anaids){
			if(uid.equals(s))
				//自己不能关注自己
				continue;
			anaid = middlewareService.findBy(checkAna,"ID",s);
			if(DataUtils.checkString(anaid) )
				//用户不存在
				continue;
			count = middlewareService.findCount(checkAttentionSql,uid,s);
			if(count > 0)
				//当前用户已经关注，不能继续关注
				continue;
			middlewareService.update(insertAttentionSql,uid,s,date);
		}
		responseInfo("1","批量添加关注分析师成功");
	}
	/**
	 * 修改偏好设置状态
	 * 第一步：判断用户id是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：更新用户验证状态为偏好设置成功状态
	 */
	public void updateAnalystState(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		String sql = " UPDATE wm_user_index SET STEP = 2 WHERE ID = ? ";
		int count = middlewareService.update(sql,uid);
		if(count==0){
			responseInfo("-3","偏好设置失败");
			return;
		}
		responseInfo("1","偏好设置成功");
	}
	
	/**
	 * 检查当前关注的分析师数量
	 * 仅适用于用户第一次登陆进入偏好设置时候检查，并且用户是不可能出现第一次进去关注普通用户的
	 * 这里就不需要判断是否关注了普通用户
	 * 主要是满足PHP端默认必须关注一个分析师
	 */
	public void checkAttentionAnalystCount(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ;
		String sql = "SELECT COUNT(*) AS COUNT FROM wm_user_friends WHERE UID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
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

	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public String getCurrencyCode() {
		return currencyCode;
	}
	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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
