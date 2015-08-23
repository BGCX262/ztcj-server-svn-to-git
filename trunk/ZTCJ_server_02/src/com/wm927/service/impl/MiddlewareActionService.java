package com.wm927.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.PageUtils;
import com.wm927.commons.ResponseCodeUtils;
import com.wm927.commons.RoleUtils;

/**
 * strutsAction方法总控制层
 * 所有的ACTION都必须继承当前类
 * @author chen
 *
 */
public class MiddlewareActionService extends MiddlewareActionServiceInterface{
	Logger logger = Logger.getLogger(MiddlewareActionService.class);
	
	protected String version =ResponseCodeUtils.DEFAULT_VERSION;//版本号
	protected String terminal = ResponseCodeUtils.DEFAULT_PORT;//终端号
	protected String rsmode = ResponseCodeUtils.DEFAULT_MODE;//返回类型
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTerminal() {
		return terminal;
	}

	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public String getRsmode() {
		return rsmode;
	}

	public void setRsmode(String rsmode) {
		this.rsmode = rsmode;
	}

	/**
	 * 验证是否为空
	 * @param map_value(key对应返回的代码,value代表检查值)
	 */
	protected boolean checkNull(Map<String,Object> map_value,String... callback){
		if(map_value==null){
			return false;
		}
		for(Entry<String, Object> map:map_value.entrySet()){
			if(DataUtils.checkString(map.getValue())){
				responseInfo("-1",map.getKey(),callback);
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param key 要显示的错误提示
	 * @param value 要判断为空的内容
	 * @param callback 回调域
	 * KEY 与 VALUE 一定要一一对应，不然会返回不对应的错误提示
	 * @return
	 */
	protected boolean checkNull (Object [] key ,Object [] value,String ...callback ){
		boolean flag = false;
		if(key == null || key.length == 0){
			responseInfo("-1","程序内部错误,验证参数时候,有空key");
			return flag ;
		}
		if(value == null || value.length == 0){
			responseInfo("-1","程序内部错误,验证参数时候,有空value");
			return flag ;
		}
		if(key.length != value.length){
			responseInfo("-1","程序内部错误,验证参数时候,key != value");
			return flag ;
		}
		int i= 0;
		for(;i<value.length;i++){
			if(DataUtils.checkString(value[i])){
				responseInfo("-1",String.valueOf(key[i]),callback);
				return flag;
			}
		}
		return true;
	}
	/**
	 * 验证用户是否存在
	 * @param middlewareService
	 */
	protected boolean checkUser(String uid,String ...callback){
		String sql = "SELECT ID from wm_user_index WHERE ID=?";
		String id = middlewareService.findBy(sql,"ID",uid);
		if(DataUtils.checkString(id)){
			responseInfo("-1","该用户不存在！",callback);
			return false;
		}
		return true;
	}
	
	/**
	 * 检查用户属于什么角色 --替换checkAnalyst
	 * @param uid
	 * @return
	 */
	protected String checkRole(String uid){
		String sql = "SELECT ROLETAG FROM wm_user_info WHERE UID = ?";
		return middlewareService.findBy(sql, "ROLETAG",uid);
	}
	
	/**
	 * 验证是否分析师(分析师包括普通分析师和APP分析师)
	 * @return
	 */
	protected boolean checkAnalystRole(String uid,String ... callback){
		String role = checkRole(uid);
		if(!RoleUtils.ANALYST_ROLE.equals(role) && !RoleUtils.APPANALYST_ROLE.equals(role)){
			responseInfo("-1","当前用户不是分析师",callback);
			return false;
		}
		return true;
	}
	/**
	 * 验证是否APP分析师
	 * @return
	 */
	protected boolean checkAppAnalystRole(String uid,String ... callback){
		if(!RoleUtils.APPANALYST_ROLE.equals(checkRole(uid))){
			responseInfo("-1","当前用户不是APP分析师",callback);
			return false;
		}
		return true;
	}
	/**
	 * 验证是否机构 机构包括APP和普通机构
	 * @return
	 */
	protected boolean checkOrgRole(String uid,String ... callback){
		String role = checkRole(uid);
		if(!RoleUtils.ORGANIZATION_ROLE.equals(role) && !RoleUtils.APPORGANIZATION_ROLE.equals(role)){
			responseInfo("-1","当前用户不是机构",callback);
			return false;
		}
		return true;
	}
	/**
	 * 验证是否APP机构
	 * @return
	 */
	protected boolean checkAppOrgRole(String uid,String ... callback){
		if(!RoleUtils.APPORGANIZATION_ROLE.equals(checkRole(uid))){
			responseInfo("-1","当前用户不是APP机构",callback);
			return false;
		}
		return true;
	}
	/**
	 * 1,检查博客是否存在,2检查评论是否存在
	 * @param id 博客id或者评论id
	 * @param tableName 博客表或者评论表(博客包括实战圈子和普通博客)
	 * @return
	 */
	protected boolean checkBlog(Object id,String tableName ,String ... callback){
		String msg = "该博客不存在";
		String nullmsg = "博客ID不能为空";
		if(DataUtils.checkString(tableName)){
			responseInfo("-1","程序内部出错",callback);
			return false;
		}
		if(tableName.indexOf("comment") != -1){
			msg = "该评论不存在";
			nullmsg = "评论ID不能为空";
		}
		if(DataUtils.checkString(id)){
			responseInfo("-1",nullmsg,callback);
			return false;
		}
		String checkBlog = "SELECT COUNT(*) FROM "+tableName+" WHERE ID = ? AND ISDELETE= 0";
		Long count = middlewareService.findCount(checkBlog,id);
		if(count == 0){
			responseInfo("-1",msg,callback);
			return false;
		}
		return true;
	}
	
	
	/**
	 * 返回当前用户跟他人的关系
	 * @param uid 用户id
	 * @param ouid 他人id
	 * @return state 
	 *   0代表我未关注你，你也未关注我--1代表我关注你，你未关注我--2代表我关注了你，你也关注了我--3代表我未关注你，你关注了我 -- 4代表索引的是自己
	 *    进行数据库的反向判断，如果我关注了你则数据库存在的条件是UID->FID----如果你关注了我则数据库存在条件是FID->UID
	 */
	public String attentionState(Object uid ,Object ouid){
		String state = "0";
		if(DataUtils.checkString(uid) || uid.equals(ouid)){
			return "4";
		}
		String checkAttentionSql = "SELECT COUNT(*) FROM wm_user_friends WHERE UID = ? AND FID = ?";
		Long attCount1 = middlewareService.findCount(checkAttentionSql,uid,ouid);
		Long attCount2 = middlewareService.findCount(checkAttentionSql,ouid,uid);
	    if(attCount1==0&&attCount2==0){
	    	state =  "0";
	    }else if(attCount1==1&&attCount2==0){
	    	state =  "1";
	    }else if(attCount1==1&&attCount2==1){
	    	state =  "2";
	    }else{
	    	state =  "3";
	    }
	    return state;
	}
	
	/**
	 * 获取个人基本信息
	 * @param uid
	 * @return
	 */
	protected Map<String,Object> findUserInfo(Object uid){
		String sql = "SELECT OPENCALL,ROLETAG,ANNOUNCEMENT,SUMMY,NICKNAME,PHOTO,USERNAME,REALNAME,UID FROM wm_user_info WHERE UID = ?";
		return middlewareService.findFirst(sql,uid);
	}
	/**
	 * 获取个人喊单部分信息
	 * @param uid
	 * 类型 0=直盘 1=内盘 2=贵金属
	 * @return
	 */
	protected Map<String,Object> findUserCallInfo(Object uid){
		String sql = "SELECT total_profit AS SUMRETURN ,month_profit AS MONTHLYRETURN , "+ 
					"	 TYPE FROM wm_user_rank WHERE uid = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,uid);
		Map<String,Object> map_value = new HashMap<String,Object>();
		//有时候数据不存在，所以返回空数据
		map_value.put("monthlyreturn", "0.00");map_value.put("sumreturn", "0.00");
		map_value.put("monthlyreturn1", "0.00");map_value.put("sumreturn1", "0.00");
		map_value.put("monthlyreturn2", "0.00");map_value.put("sumreturn2", "0.00");
		
		for(Map<String,Object> map:list_value){
			if("1".equals(map.get("type"))){
				map_value.put("monthlyreturn1", map.get("monthlyreturn"));map_value.put("sumreturn1", map.get("sumreturn"));
			}else if("2".equals(map.get("type"))){
				map_value.put("monthlyreturn2", map.get("monthlyreturn"));map_value.put("sumreturn2", map.get("sumreturn"));
			}else{
				map_value.put("monthlyreturn", map.get("monthlyreturn"));map_value.put("sumreturn", map.get("sumreturn"));
			}
		}
		return map_value;
	}
	/**
	 * 获取个人公共部分信息(粉丝数，粉丝变动数等)
	 * @param uid
	 * @return
	 */
	protected Map<String,Object> findUserComInfo(Object uid){
		String sql = "SELECT ODDS,MAXPROFIT,HDSUM,VIEWSUM,PLOTSUM,QUESTIONSUM,FLOWCOUNT,BROWSENUMBER,FANSSUM,ATTENTIONSUM,BLOGSUM,FANSCHANGENUBER,COMMENTCHANGENUMBER,PRAISECHANGENUMBER,NOTICECHANGENUMBER,GOLD,INTEGRAL FROM wm_user_communityInfo WHERE UID = ?";
		return middlewareService.findFirst(sql,uid);
	}
	
	/**
	 * 检查是否购买了分析师的策略服务
	 * @return
	 */
	protected boolean checkByCircleService(Object uid ,Object ouid){
		String sql = "SELECT COUNT(*) FROM wm_user_analyst_room WHERE UID = ? AND ANAID = ? AND PASSTIME > ?";
		Long count = middlewareService.findCount(sql,uid,ouid,DateUtils.getCurrentTime());
		if(count == 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 检查是否购买了分析师的单条策略
	 * @return
	 */
	protected boolean checkByCircle(Object uid ,Object ouid,Object cid){
		String sql = "SELECT COUNT(*) FROM wm_user_analyst_circle_sell WHERE UID = ? AND ANAID = ? AND CID = ?";
		Long count = middlewareService.findCount(sql,uid,ouid,cid);
		if(count == 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 插入动态表
	 * @param uid 	用户id
	 * @param id 	动态id
	 * @param type 	动态类型
	 * @param msg 若插入失败记录日志
	 * @return dycount 动态ID
	 */
	protected int insertDynamic(Object uid,Object ouid,Object id,Object type,Object msg){
		// 插入动态
		String dynamicSql = "INSERT INTO wm_user_dynamic (UID,OUID,TYPE,RID,ADDIP)VALUES(?,?,?,?,?)";
		int dyCount = middlewareService.update(dynamicSql,uid,ouid,type,id,getIpAddr());
		if(dyCount == 0){
			logger.info(msg);
		}
		return dyCount ;
	}
	/**
	 * 获取博客一级或者二级评论
	 * 获取博客一级评论 检查博客时则传入博客id，检查评论时则传入评论id
	 * @param type       0代表博客评论，1代表微博评论 默认0
	 * @param blogid     博客id
	 * @param commentid  评论id
	 * @param lastid     最后的博客评论id
	 * @param size       索引的数目
	 */
	protected Object getCommentLevelList(String type,Object blogid,Object commentid,String lastid,String size){
		List<Map<String,Object>> list_error = new ArrayList<Map<String,Object>>();
		String where = "COMMENTBLOGID="+blogid;
		String level = "1";
		String blogTable = "wm_blog_info";
		String blogCommentTable = "wm_blog_comment";
		if("1".equals(type)){
			blogTable = "wm_blog_micro_info";
			blogCommentTable = "wm_blog_micro_comment";
		}
		if(!DataUtils.checkString(blogid)){
			if(!checkBlog(blogid,blogTable))
				return list_error;
		}else{
			//二级评论，检查一级评论是否存在
			if(!checkBlog(commentid,blogCommentTable))
				return list_error;
			level = "2";
			where = "COMMENBLOGID="+commentid;
		}	
		if(!DataUtils.checkString(lastid)){
			where += " AND ID < "+lastid;	
		}
		List<Map<String,Object>> list_value = null;
		if(DataUtils.checkString(commentid)){
			Long commentCount = returnCommentCount(blogCommentTable,level,where);
			Long count = commentCount - PageUtils.getPageCount(size);
			commentCount = count > 0 ? count : 0 ; 
			List<Map<String,Object>> list_value1 = returnCommentLevel(blogCommentTable,level,where,size);
			
			Map<String,Object> map_value = new HashMap<String,Object>();
			map_value.put("data", list_value1);
			map_value.put("lavecomment", commentCount+"");
			list_value = new ArrayList<Map<String,Object>>();
			list_value.add(map_value);
		}else{
			list_value = returnCommentLevel(blogCommentTable,level,where,size);
		}
		return list_value;
		
	}
	
	/**
	 * 获取评论数据
	 * @param tableName
	 * @param level
	 * @param where
	 * @param size
	 * @return
	 */
	private List<Map<String,Object>> returnCommentLevel(String tableName,String level,String where,String size){
		String commentSql = "SELECT UID ,ID AS COMMENTID,COMMENTCONTENT,COMMENTLEVEL,DATE_FORMAT(COMMENTDATE ,'%Y-%m-%d %H:%i:%s') AS 'COMMENTDATE'"
				+" FROM  "+tableName
				+"  WHERE ISDELETE = 0 AND "+where+" AND COMMENTLEVEL = "+level+" ORDER BY ID DESC LIMIT " + PageUtils.getPageCount(size);
		List<Map<String,Object>> list_value = middlewareService.find(commentSql);
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map: list_value){
			map_value = findUserInfo(map.get("uid"));
			map.putAll(map_value);
		}
		return list_value;
	}
	
	/**
	 * 获取评论总数
	 * @param level
	 * @param where
	 * @return
	 */
	private Long returnCommentCount(String tableName,String level,String where){
		String commentCountSql = "SELECT COUNT(*) FROM  "+tableName
				+"  WHERE ISDELETE = 0 AND "+where+" AND COMMENTLEVEL = "+level;
		Long commentCount = middlewareService.findCount(commentCountSql);
		return commentCount;
	}
	
}
