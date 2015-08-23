package com.wm927.action.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

import com.wm927.commons.DataUtils;
import com.wm927.commons.DateUtils;
import com.wm927.commons.Decoder;
import com.wm927.commons.SensitiveWordFilter;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 微博评论
 * @author chen
 *
 */
public class MicroBlogComment extends MiddlewareActionService{
	private static final Logger logger = Logger.getLogger(MicroBlogComment.class);
	//博客id
	private String blogid;
	//用户id
	private String uid;
	//评论id
	private String comid;
	//评论内容
	private String comcontent;
	//评论人id
	private String callback;
	private String commentid;
	
	/**
	 * 以评论ID作为判断依据：若评论ID不为空，则为2级评论，代表回复的是评论，否则回复的是博客评论
	 * 第一步：验证字段是否为空
	 * 第二步：验证用户和博客是否存在
	 * 第三步：将当前博客评论插入wm_blog_comment评论表
	 * 第四步：将wm_blog_info表中的博客评论数自动加1
	 * 第五步：将wm_user_communityInfo表中的博客评论状态自动加1
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","博客ID不能为空","评论内容不能为空"},new Object[]{uid,blogid,comcontent}))
			return ; 
		
		if(!checkUser(uid)){
			return;
		}
		if(!checkBlog(blogid,"wm_blog_micro_info")){
			return;
		}
		
		String level = "1";
		if(DataUtils.checkString(comid) ){
			comid = null;
		}else{
			String checkcoment = "SELECT COUNT(*) FROM wm_blog_micro_comment WHERE ID = ? AND ISDELETE = 0";
			Long commentcount = middlewareService.findCount(checkcoment,comid);
			if(commentcount==0){
				responseInfo("-3","回复的评论不存在");
				return;
			}
			level="2";
		}
		String sql = "INSERT INTO wm_blog_micro_comment(UID,COMMENTBLOGID,COMMENTCONTENT,COMMENTDATE,COMMENBLOGID,COMMENTLEVEL,ISDELETE,TERMINAL)VALUES(?,?,?,?,?,?,?,?)";
		String currenttime = DateUtils.getCurrentTime();
		filter();
		int insertCount = middlewareService.update(sql,uid,blogid,comcontent,currenttime,comid,level,0,terminal);
		if(insertCount==0){
			logger.info("INSERT wm_blog_comment false ON this class --> "+this.getClass().getSimpleName());
			responseInfo("-3","插入微博评论失败");
			return;
		}
		//更新博客评论数,如果是二级评论，则不更新博客评论数
		if(level.equals("1")){
			String updateBlogComment ="UPDATE wm_blog_micro_info SET COMMENTNUMBER=COMMENTNUMBER+1 WHERE ID = ?";
			int updateCount = middlewareService.update(updateBlogComment,blogid);
			if(updateCount==0){
				logger.info("微博评论 ----"+"UPDATE wm_blog_info false ON this class --> "+this.getClass().getSimpleName() +" 更新微博评论数失败");
			} 
			//更新评论状态
			String getuid = "SELECT UID FROM wm_blog_micro_info WHERE ID = ?";
			String objid = middlewareService.findBy(getuid, "UID",blogid);
			String updateCommentState = "UPDATE wm_user_communityInfo SET MICROCOMMENTCHANGENUMBER = MICROCOMMENTCHANGENUMBER+1 WHERE UID = ?";
			int commentstatecount = middlewareService.update(updateCommentState,objid);
			if(commentstatecount==0){
				logger.info("微博评论 ----"+"UPDATE wm_blog_info false ON this class --> "+this.getClass().getSimpleName() +" 更新微博状态失败");
			}
		}
		
		List<Object> list_value = new ArrayList<Object> ();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("commentid", insertCount);
		map.put("currenttime", currenttime);
		list_value.add(map);
		responseInfo("1","更新评论成功",list_value);
	}
	
	
	/**
	 * 删除博客评论(逻辑删除，不是物理删除 将wm_blog_comment 与wm_blog_info中ISDELETE字段)
	 * 第一步：判断字段是否为空
	 * 第二步：判断当前博客是否存在
	 * 第三步：删除博客评论
	 * 第四部：删除评论当前评论的所有评论(2级评论)
	 */
	public void deleteComment(){
		if(!checkNull(new Object[]{"用户ID不能为空","评论ID不能为空"},new Object[]{uid,commentid},callback))
			return ;
		
		if(!checkUser(uid,callback)){
			return;
		}
		
		String sql = "SELECT COUNT(*) FROM wm_blog_micro_comment WHERE UID = ? AND ID = ? AND ISDELETE = 0";
		Long count1 = middlewareService.findCount(sql,uid,commentid);
		Long count2  ;
		if(count1 == 0){
			//博客的主人可以删除评论
			String findBlogUser = "SELECT COMMENTBLOGID FROM  wm_blog_micro_comment WHERE ID = ? AND  ISDELETE = 0";
			String blogid = middlewareService.findBy(findBlogUser, "COMMENTBLOGID",commentid);
			String otherCheck = "SELECT COUNT(*) FROM wm_blog_micro_info WHERE ID = ? AND UID = ? AND  ISDELETE = 0";
			count2 = middlewareService.findCount(otherCheck,blogid,uid);
			if(count2 == 0){
				responseInfo("-3","当前评论不存在",callback);
				return;
			}
		}
		String deleteComment = "";
		int delCount = 0 ;
		if(count1 == 0){
			deleteComment = "UPDATE wm_blog_micro_comment SET ISDELETE=1 WHERE ID = ? ";
			delCount = middlewareService.update(deleteComment,commentid);
		}else{
			deleteComment = "UPDATE wm_blog_micro_comment SET ISDELETE=1 WHERE UID = ? AND ID = ? ";
			delCount = middlewareService.update(deleteComment,uid,commentid);
		}
		
		if(delCount == 0){
			responseInfo("-3","删除评论失败",callback);
			return;
		}
		String findBlogId = "SELECT COMMENTBLOGID,COMMENTLEVEL FROM wm_blog_micro_comment WHERE ID = ?";
		Map<String,Object> com_map = middlewareService.findFirst(findBlogId,commentid);
		if("1".equals(com_map.get("commentlevel"))){
			//如果是一级评论，则需要将博客评论数-1
			String updateBlogCommentCount = "UPDATE wm_blog_micro_info SET COMMENTNUMBER = COMMENTNUMBER -1 WHERE ID = ?";
			int updateCount = middlewareService.update(updateBlogCommentCount,com_map.get("commentblogid"));
			if(updateCount == 0){
				logger.info("删除微博评论 ----"+"UPDATE wm_blog_micro_comment false ON this class --> "+this.getClass().getSimpleName()+" 更新微博评论数-1失败");
			}
		}
		//删除当前评论所关联的评论
		String otherComment = "UPDATE wm_blog_micro_comment SET ISDELETE=1 WHERE COMMENBLOGID = ? ";
		middlewareService.update(otherComment,commentid);
		responseInfo("1","删除评论成功",callback);
	}
	
	private void filter(){
		String regex = "<javascript>[^>]*</javascript>";
		Pattern pattern = Pattern.compile(regex);
		Matcher mater = pattern.matcher(comcontent);
		comcontent = mater.replaceAll("");//替换带有脚本的内容
		comcontent = SensitiveWordFilter.doFilter(comcontent);//过滤敏感词
		
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


	public String getComid() {
		return comid;
	}


	public void setComid(String comid) {
		this.comid = comid;
	}


	public String getComcontent() {
		return comcontent;
	}


	public void setComcontent(String comcontent) {
		if(!DataUtils.checkString(comcontent))
			comcontent = Decoder.decode(comcontent);
		this.comcontent = comcontent;
	}


	public String getCallback() {
		return callback;
	}


	public void setCallback(String callback) {
		this.callback = callback;
	}


	public String getCommentid() {
		return commentid;
	}


	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}
	
	
}
