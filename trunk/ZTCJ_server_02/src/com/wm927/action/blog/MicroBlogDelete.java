package com.wm927.action.blog;

import org.apache.log4j.Logger;

import com.wm927.service.impl.MiddlewareActionService;

public class MicroBlogDelete extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(MicroBlogDelete.class);
	private String uid;
	private String blogid;
	private String type = "0";//0代表微博，1代表观点,2代表策略，3代表秘笈
	/**
	 * 删除微博，删除秘笈，删除策略
	 */
	public void execute(){
		String sql = "UPDATE wm_blog_micro_info SET ISDELETE=1 WHERE ID = ? AND UID = ?";
		if(!"0".equals(type)){
			sql = "UPDATE wm_blog_view SET ISDELETE=1 WHERE ID = ? AND UID = ?";
		}
		int count = middlewareService.update(sql,blogid,uid);
		if( count == 0){
			responseInfo("-1","删除失败");
			return ; 
		}
		//删除动态表
		String dySql = "DELETE FROM wm_user_dynamic WHERE RID = ?";
		int dyCount = middlewareService.update(dySql,blogid);
		if(dyCount == 0){
			logger.info("删除微博时候，删除动态表失败 --->"+blogid);
		}
		responseInfo("1","删除成功");
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getBlogid() {
		return blogid;
	}

	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}
	
	
}	
