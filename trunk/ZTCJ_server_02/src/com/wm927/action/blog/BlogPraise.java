package com.wm927.action.blog;

import com.wm927.service.impl.MiddlewareActionService;

public class BlogPraise extends MiddlewareActionService{
	private String uid;
	private String blogid;
	private String praisetype = "0";//赞(0) 贬(1)
	/**
	 * 点赞，赞数+1
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","博客ID不能为空"},new Object[]{uid,blogid}))
			return ; 
		
		if(!checkUser(uid)){
			return;
		}
		if(!checkBlog(blogid,"wm_blog_info")){
			return;
		}
		//判断是否已经点赞，已经点赞则返回错误，否则插入
		String checkpraise = "SELECT COUNT(*) FROM wm_blog_blogPraise where UID = ? AND BLOGID = ? ";
		Long checkpraisecount = middlewareService.findCount(checkpraise,uid,blogid);
		if(checkpraisecount> 0){
			responseInfo("-3","已经点赞");
			return;
		}else{
			String praisesql = "INSERT INTO wm_blog_blogPraise (UID,BLOGID,TYPE,ADDIP)VALUES(?,?,?,?)";
			int praiseCount = middlewareService.update(praisesql,uid,blogid,praisetype,getIpAddr());
			if(praiseCount==0){
				responseInfo("-3","点赞失败");
				return;
			}
		}
		//更新博客表的赞数量
		String sql = "UPDATE wm_blog_info SET PRAISENUMBER = PRAISENUMBER+1 WHERE ID = ?";
		int count = middlewareService.update(sql,blogid);
		if(count==0){
			responseInfo("-3","更新赞数失败");
			return;
		}
		String getuid = "SELECT UID FROM wm_blog_info WHERE ID = ?";
		String objid = middlewareService.findBy(getuid, "UID",blogid);
		String updateState = "UPDATE wm_user_communityInfo SET PRAISECHANGENUMBER = PRAISECHANGENUMBER+1 where UID = ?";
		int commentstatecount = middlewareService.update(updateState,objid);
		if(commentstatecount==0){
			responseInfo("-3","更新赞数状态失败");
			return;
		}
		responseInfo("1","更新赞数成功");
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
	public String getPraisetype() {
		return praisetype;
	}
	public void setPraisetype(String praisetype) {
		this.praisetype = praisetype;
	}
	
}
