package com.wm927.action.msg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 信息中心博客评论
 * @author chen (11-08更新)
 * 根据当前登录人在评论表最近的几条评论来查找评论和博客
 *  *307205
 *
 */
public class BlogCommentList extends MiddlewareActionService{
	private Logger log = Logger.getLogger(BlogCommentList.class);

	//每页数量 -->默认5条
	private String number;
	//当前页 -->默认第一页
	private String page;
	private String uid;
	private String commentid;
	
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		//用户的博客内容与评论内容
		String blogInfo = " SELECT blog.ID blogid,blog.BLOGTITLE,blog.BLOGCONTENT,blog.IMGLIST, DATE_FORMAT(blog.ADDTIME,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME', blog.COMMENTNUMBER,blog.PRAISENUMBER, " +
							" comment.ID AS commentid,DATE_FORMAT(comment.COMMENTDATE,'%Y-%m-%d %H:%i:%s') AS COMMENTADDTIME, comment.UID AS commentuid,comment.COMMENTCONTENT "+
							"  FROM wm_blog_info blog  RIGHT JOIN wm_blog_comment comment ON blog.ID = comment.COMMENTBLOGID WHERE blog.UID = "+uid +" AND comment.ISDELETE = 0 AND comment.COMMENTLEVEL = 1 ORDER BY comment.ID DESC";
		//发布博客人的信息
		String userBlogInfoSql = " SELECT uinfo.PHOTO AS blogphoto, uinfo.NICKNAME AS blogname,uinfo.USERNAME AS blogusername FROM wm_user_info uinfo WHERE UID = ?";
		//博客评论人的信息
		String userPraiseInfoSql = " SELECT uinfo.PHOTO AS commentphoto, uinfo.NICKNAME AS commentname,uinfo.USERNAME AS commentusername FROM wm_user_info uinfo WHERE uinfo.UID = ?";
		
		List<Map<String,Object>> list_value = findPageInfo(page, number, blogInfo);
		Map<String,Object> userBlogInfo = middlewareService.findFirst(userBlogInfoSql,uid);
		//分析师鲜花数//普通用户鲜花为0
		String countflow = "SELECT FLOWCOUNT FROM wm_user_communityInfo WHERE UID = ?";
		String count = "";
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map:list_value){
			map.putAll(userBlogInfo);//加入发布博客人的信息
			map_value = middlewareService.findFirst(userPraiseInfoSql,map.get("commentuid"));
			map.putAll(map_value);//加入发布评论人的信息
			count =  middlewareService.findBy(countflow,"FLOWCOUNT",map.get("commentuid"));
			map.put("flowcount", count);
			map.put("blogcontent", imgUrl(map.get("blogcontent")+""));
		}
		
		Map<String,Object> pageInfoMap = findPageSize(page, number, blogInfo);
		responseInfo("1","返回数据成功",list_value,pageInfoMap);
		
	}
	/**
	 * 传入评论ID，返回二级评论信息
	 */
	public void otherCommentList(){
		if(!checkNull(new Object[]{"评论ID不能为空"},new Object[]{commentid}))
			return ; 
		String sql = "SELECT bcom.COMMENTCONTENT,bcom.COMMENTDATE,bcom.ID,uinfo.UID,uinfo.REALNAME,uinfo.PHOTO "+
					"	FROM  wm_blog_comment bcom LEFT JOIN wm_user_info uinfo ON uinfo.UID=bcom.USERID"+
					"	WHERE bcom.COMMENBLOGID="+commentid+" AND bcom.COMMENTLEVEL=2 AND bcom.ISDELETE=0 ORDER BY  COMMENTDATE DESC";
		responseInfo(page,number,"1","返回成功",sql);
	}
	private String imgUrl(String content){
		String regex = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
		return content.replaceAll(regex, "");
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCommentid() {
		return commentid;
	}
	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}
	
}