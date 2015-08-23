package com.wm927.action.article;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.dbutils.ContextHolder;
import com.wm927.service.impl.MiddlewareActionService;

public class WebArticleComment extends MiddlewareActionService {
	private static final Logger logger = Logger.getLogger(WebArticleComment.class);
	private String target_Table = "";//哪种资讯的表
	private String uid;//用户ID(30309)
	private String atyp;//资讯类型：0路透，1专家评论、2机构评论、3道琼斯、4智库
	private String content;//评论内容
	private String aid;//资讯ID
	private String cid;//评论ID
	private String level;//评论级别
	private String commentState = "0";// 默认已审核
	private String commentuserip = getIpAddr();//评论人IP
	private String keyWord = "";//关键词默认为空
	private String alogdate = new Date().toString();//赞时间
	
	private String number;//返回条数
	private String lastid;//最后一条ID
	/**
	 * action=30310
	 * 添加文章评论
	 * 返回：评论生成后的ID
	 */
	public void comment() {
		ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);//动态切换至SqlServer数据库 
		int result = 0;
		//验证参数
		if(!Validator()){
			logger.error("Action=30310,参数错误");
			responseInfo("2","参数个数或参数内容不正确！");
			return;
		}
		//赞开始
		String sql = "INSERT INTO "+target_Table+"(COMMENTARTICLEID,COMMENTCONTENT,COMMENTDATE," +
				"COMMENTREPLYID,KEYWORD，COMMENTUSERIP，COMMENTUID，COMMENTSTATE，ADMINREMARK，LEVEL) " +
				"VALUES(?,?,?,?,?,?)";
		result = middlewareService.update(sql,new Object[]{aid,content,alogdate,cid,keyWord,commentuserip,
				uid,commentState,"",level});
		if(result > 0){
			String temp_sql = String.format("SELECT ID FROM %s WHERE COMMENTUID = ? AND COMMENTDATE = ?",target_Table);
			Map<String,Object> articleId = middlewareService.findFirst(temp_sql,new Object[]{uid,alogdate});
			responseInfo("0","数据返回成功",articleId);
		}else{
			responseInfo("3","赞失败~~");
		}
	}
	
	/**
	 * action=30311
	 * 获取评论列表
	 * 返回：{
		   "error":"0", 
		   "data":[{"data":[],//评论数据 
		            "acommentcount":0 //评论数  } ],
		   "msg":""
  		}
	 */
	public void getCommentList() {
		ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);//动态切换至SqlServer数据库 
		//验证参数
		if(!Validator()){
			logger.error("Action=30311,参数错误");
			responseInfo("2","参数个数或参数内容不正确！");
			return;
		}
		String sql = "SELECT * FROM "+ target_Table +" WHERE COMMENTARTICLEID = ? AND id <= ? LIMIT ?";
		List<Map<String,Object>> result = middlewareService.find(sql,new Object[]{aid,lastid,number});
		if(result != null){
			responseInfo("0","",result);
		}else{
			responseInfo("3","获取评论列表失败");
		}
	}
	
	/**
	 * action=30310
	 * 验证参数赋值
	 */
	public Boolean Validator() {
		uid = getParameter("uid");
		atyp = getParameter("atyp");
		aid = getParameterOrDefault("aid", "");
		cid = getParameterOrDefault("cid", "");//非必填
		if(DataUtils.checkString(uid)){
			return false;
		}
		if(DataUtils.checkString(atyp)){
			return false;
		}else{
			setTargetTable();
		}
		//cid aid只能传其一
		if(DataUtils.checkString(aid) && DataUtils.checkString(cid)){
			return false;
		}else if(!DataUtils.checkString(aid) && !DataUtils.checkString(cid)){
			return false;
		}
		//计算评论级别
		if(!DataUtils.checkString(aid)){
			level = "0";
		}
		if(!DataUtils.checkString(cid)){
			level = "1";
		}
		return true;
	}
	
	/**
	 * action=30311
	 * 验证参数赋值
	 */
	public Boolean Validator2(){
		uid = getParameter("uid");
		atyp = getParameter("atyp");
		aid = getParameterOrDefault("aid", "");
		number = getParameterOrDefault("number","10");
		lastid = getParameter("lastid");
		if(DataUtils.checkString(uid)){
			return false;
		}
		if(DataUtils.checkString(atyp)){
			return false;
		}else{
			setTargetTable();
		}
		if(DataUtils.checkString(aid)){
			return false;
		}
		if(DataUtils.checkString(lastid)){
			String temp_sql = "SEELCT MAX(ID) AS max FROM wm_article_wm_comment";
			lastid = middlewareService.findFirst(temp_sql).get("max").toString();
			return false;
		}
		return true;
	}
	
	public void setTargetTable(){
		if(atyp.equals("0")){//路透
			target_Table = "wm_article_reuters_comment";
		}else if(atyp.equals("1")){//专家评论
			target_Table = "wm_article_expert_comment";
		}else if(atyp.equals("2")){//机构评论
			target_Table = "wm_article_agency_comment";
		}else if(atyp.equals("3")){//道琼斯
			target_Table = "wm_article_dowjones_comment";
		}else if(atyp.equals("4")){//智库
			target_Table = "wm_article_wm_comment";
		}
	}
}
