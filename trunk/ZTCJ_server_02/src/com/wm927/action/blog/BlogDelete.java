package com.wm927.action.blog;

import org.apache.log4j.Logger;

import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 删除博客
 * @author chen
 * 修改于2013-10-22
 */
public class BlogDelete extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(BlogDelete.class);
	//用户id
	private String uid;
	//博客id
	private String blogid;
	private String type;//0代表删除博客，1代表删除微博，2代表删除观点、删除策略、服务
	/**
	 * 删除博客(逻辑删除，不是物理删除，只需将表中的ISDELETE字段改为已删除)
	 * 第一步：判断字段是否为空
	 * 第二步：判断博客是否存在并且是否自己的博客
	 * 第三步：删除博客
	 * 第四步：删除与博客相关的评论
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","博客ID不能为空"},new Object[]{uid,blogid}))
			return ;
		
		if(!checkUser(uid)){
			return;
		}
		String checkmyblog = "SELECT COUNT(*) FROM wm_blog_info WHERE ID = ? AND UID = ?";
		Long checkCount  = middlewareService.findCount(checkmyblog,blogid,uid);
		if(checkCount==0){
			responseInfo("-3","不是自己的博客不能删除");
			return;
		}
		//将博客是否删除该为已删除
		String sql = "UPDATE wm_blog_info SET ISDELETE = 1 WHERE ID = ?";
		int updateBlogCount = middlewareService.update(sql,blogid);
		if(updateBlogCount==0){
			logger.info("UPDATE wm_blog_info false ON this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","博客已删除");
			return;
		}
		String findBlogInfo = "SELECT ISPUBLIC FROM wm_blog_info WHERE ID = ?";
		String ispublic = middlewareService.findBy(findBlogInfo,"ISPUBLIC",blogid);
		
		//0代表未发表，1代表已发表
		//如果是未发表则不用做下面操作
		if("0".equals(ispublic)){
			responseInfo("1","博客已删除");
			return ;
		}
		//更新博客总数-1
		String updateCom = "UPDATE wm_user_communityInfo SET BLOGSUM = BLOGSUM-1 WHERE UID = ?";
		int updateBlogComCount = middlewareService.update(updateCom,uid);
		if(updateBlogComCount == 0){
			logger.info("更新发布博客总数-1失败 ----"+this.getClass().getSimpleName());
		}
		//删除动态表中当前博客记录
		String deleteDynamic = "DELETE FROM wm_user_dynamic WHERE UID = ? AND  RID = ?";
		int dynamicCount = middlewareService.update(deleteDynamic,uid,blogid);
		if(dynamicCount == 0){
				logger.info("删除动态表中当前博客记录失败 ----"+this.getClass().getSimpleName());
			}
		responseInfo("1","博客已删除");
		
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


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
