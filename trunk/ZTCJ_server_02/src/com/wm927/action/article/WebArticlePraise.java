package com.wm927.action.article;

import java.util.Date;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.dbutils.ContextHolder;
import com.wm927.service.impl.MiddlewareActionService;

public class WebArticlePraise extends MiddlewareActionService {
	private static final Logger logger = Logger.getLogger(WebArticlePraise.class);
	private String target_Table = "";//哪种资讯的表
	private String uid;//用户ID(30309)
	private String atyp;//资讯类型：0路透，1专家评论、2机构评论、3道琼斯、4智库
	private String ptype;//操作类型：赞1，取消赞0
	private String aid;//资讯ID
	private String cid;//评论ID
	private String alogdate = new Date().toString();//赞时间
	
	/**
	 * action=30309
	 * 添加文章赞 
	 * 返回：空
	 */
	public void praise() {
		ContextHolder.setCustomerType(ContextHolder.DATA_SOURCE2);//动态切换至SqlServer数据库 
		int result = 0;
		//验证参数
		if(!Validator()){
			logger.error("Action=30309,参数错误");
			responseInfo("2","参数个数或参数内容不正确！");
			return;
		}
		//赞开始
		String sql = "INSERT INTO "+target_Table+"(USERID,ARTICLEID,COMMENTID,TYPE,ARTICLETIME,ARTICLEIP) " +
				"VALUES(?,?,?,?,?,?)";
		result = middlewareService.update(sql, new Object[]{uid,aid,cid,alogdate,getIpAddr()});
		if(result > 0){
			responseInfo("0","数据返回成功");
		}else{
			responseInfo("3","赞失败~~");
		}
	}
	
	/**
	 * action=30309
	 * 验证参数赋值
	 */
	public Boolean Validator() {
		uid = getParameter("uid");
		atyp = getParameter("atyp");
		ptype = getParameter("ptype");
		aid = getParameterOrDefault("aid", "");
		cid = getParameterOrDefault("cid", "");//非必填
		if(DataUtils.checkString(uid)){
			return false;
		}
		if(DataUtils.checkString(atyp)){
			return false;
		}else{
			if(atyp.equals("0")){//路透
				target_Table = "wm_article_reuters_praise";
			}else if(atyp.equals("1")){//专家评论
				target_Table = "wm_article_expert_praise";
			}else if(atyp.equals("2")){//机构评论
				target_Table = "wm_article_agency_praise";
			}else if(atyp.equals("3")){//道琼斯
				target_Table = "wm_article_dowjones_praise";
			}else if(atyp.equals("4")){//智库
				target_Table = "wm_article_wm_praise";
			}
			
		}
		if(DataUtils.checkString(ptype)){
			return false;
		}
		//cid aid只能传其一
		if(DataUtils.checkString(aid) && DataUtils.checkString(cid)){
			return false;
		}else if(!DataUtils.checkString(aid) && !DataUtils.checkString(cid)){
			return false;
		}
		return true;
	}
	
}
