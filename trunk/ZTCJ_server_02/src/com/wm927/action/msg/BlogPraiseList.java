package com.wm927.action.msg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 赞记录及列表
 * 根据当前登录人在wm_blog_blogPraise表最近的几次被赞来查找评论和博客
 * @author chen (最近更新日期：11-08)
 *
 */
public class BlogPraiseList extends MiddlewareActionService{
	Logger log = Logger.getLogger(BlogPraiseList.class.getName());
	//每页数量 -->默认5条
	private String number;
	//当前页 -->默认第一页
	private String page;
	private String uid;
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		if(!checkUser(uid))
			return;
		//用户的博客内容与赞内容
		String blogInfo = " SELECT blog.ID blogid,blog.BLOGTITLE,blog.BLOGCONTENT,blog.IMGLIST, DATE_FORMAT(blog.ADDTIME,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME', blog.COMMENTNUMBER,blog.PRAISENUMBER, " +
							" upraise.ID AS praiseid,DATE_FORMAT(upraise.ADDTIME,'%Y-%m-%d %H:%i:%s') AS PRAISEADDTIME, upraise.UID AS praiseuid "+
							"  FROM wm_blog_info blog  RIGHT JOIN wm_blog_blogPraise upraise ON blog.ID = upraise.BLOGID WHERE blog.UID = "+uid +" ORDER BY upraise.ID DESC";
		//发布博客人的信息
		String userBlogInfoSql = " SELECT uinfo.PHOTO AS blogphoto, uinfo.NICKNAME AS blogname,uinfo.USERNAME AS blogusername FROM wm_user_info uinfo WHERE UID = ?";
		//赞博客人的信息
		String userPraiseInfoSql = " SELECT uinfo.PHOTO AS praisephoto, uinfo.NICKNAME AS praisename,uinfo.USERNAME AS praiseusername FROM wm_user_info uinfo WHERE uinfo.UID = ?";
		
		List<Map<String,Object>> list_value = findPageInfo(page, number, blogInfo);
		Map<String,Object> userBlogInfo = middlewareService.findFirst(userBlogInfoSql,uid);
		//分析师鲜花数//普通用户鲜花为0
		String countflow = "SELECT FLOWCOUNT FROM wm_user_communityInfo WHERE UID = ?";
		String count = "";
		Map<String,Object> map_value = new HashMap<String,Object>();
		for(Map<String,Object> map:list_value){
			map.putAll(userBlogInfo);//加入发布博客人的信息
			map_value = middlewareService.findFirst(userPraiseInfoSql,map.get("praiseuid"));
			map.putAll(map_value);//加入发布评论人的信息
			count =  middlewareService.findBy(countflow,"FLOWCOUNT",map.get("praiseuid"));
			map.put("flowcount", count);
			map.put("blogcontent", imgUrl(map.get("blogcontent")+""));
		}
		
		Map<String,Object> pageInfoMap = findPageSize(page, number, blogInfo);
		responseInfo("1","返回数据成功",list_value,pageInfoMap);
		
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
	
}
