package com.wm927.action.blog;

import com.wm927.service.impl.MiddlewareActionService;

/**
 * 修改博客权限
 * @author chen
 *
 */
public class BlogUpdatePurview extends MiddlewareActionService{
	private String blogid;
	private String uid;
	private String purview;
	/**
	 * 修改博客权限
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","修改博客权限不能为空","博客不能为空"},new Object[]{uid,purview,blogid}))
			return ; 
		
		if(!checkUser(uid))
			return;
		if(!checkBlog(blogid,"wm_blog_info"))
			return;
		String sql = "UPDATE wm_blog_info SET PURVIEWID = ? WHERE UID = ? AND ID = ?";
		int count = middlewareService.update(sql,purview,uid,blogid);
		if(count == 0){
			responseInfo("-3","修改博客权限失败!");
			return;
		}
		responseInfo("1","修改博客权限成功!");
	}
	public String getBlogid() {
		return blogid;
	}
	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getPurview() {
		return purview;
	}
	public void setPurview(String purview) {
		this.purview = purview;
	}
	
}
