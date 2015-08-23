package com.wm927.action.blog;

import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 博客列表
 * @author chen
 * 修改于2013-10-22
 */
public class BlogList extends MiddlewareActionService{
	//传入用户ID
	private String uid;
	//关注人的id
	private String ouid;
	//页码--默认第一页
	private String page;
	//每页显示条数--默认15条
	private String number;
	//博客类型(1 黄金 2 白银 3 外汇 4 其它5草稿箱)
	private String type;
	//博客id
	private String blogid;
	//评论人id
	private String commentid;
    /**
     * 博客列表
     * 第一步：判断字段是否为空
     * 第二步：判断博客和用户是否存在
     * 第三步：判断博客类型是否为空(为空则显示全部博客，否则显示详细的分类博客)
     * 第四步：判断博客类型是否为草稿箱(为草稿箱则SQL条件不同，显示的数据也不同)
     */
	public void execute(){
		
		if(!checkNull(new Object[]{"用户ID不能为空","他人用户ID不能为空"},new Object[]{uid,ouid}))
			return ; 
		
		if(!checkUser(uid))
			return;
		if(!checkUser(ouid))
			return;
		if(DataUtils.checkString(number)){
			number = "15";
		}
		if(!uid.equals(ouid)){
			//不是本人则不能查看草稿箱内容,以及权限为最仅对自己开放的内容
			otherBlogList();
		}
		String sql = "";
		if(DataUtils.checkString(type)){
			sql = "SELECT ID,BLOGTITLE,IMGLIST,CLASSID, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',COMMENTNUMBER,BROWSENUMBER,BLOGSUMMY FROM wm_blog_info WHERE UID = "+ouid+" AND ISPUBLIC=1 AND ISDELETE = 0  ORDER BY ID DESC";
		
		}else{
			if(type.equals("5")){
				//获取草稿箱
				sql = "SELECT ID,BLOGTITLE,IMGLIST,CLASSID, DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',COMMENTNUMBER,BROWSENUMBER,BLOGSUMMY FROM wm_blog_info where UID = "+ouid+" and ISPUBLIC=0 and ISDELETE=0  ORDER BY ID DESC";
			}else{
				//可能一次获取多个分类
				String types[] = type.split(",");
				String wheretype = "";
				for (String t:types){
					wheretype += "OR blog.CLASSID = " +t +" ";
				}
				wheretype = wheretype.substring(2);
				sql = "SELECT blog.ID,blog.IMGLIST,blog.CLASSID,blog.BLOGTITLE, DATE_FORMAT(blog.ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',blog.COMMENTNUMBER,blog.BROWSENUMBER,blog.BLOGSUMMY "+
						"	FROM wm_blog_info blog"+
						"	LEFT JOIN wm_blog_articleClass blogart ON blog.CLASSID=blogart.ID"+
						"	where UID = "+ouid+" and ("+wheretype+" ) AND blog.ISPUBLIC = 1 AND blog.ISDELETE = 0  ORDER BY blog.ADDTIME DESC";
				}
		}
		responseInfo(page,number,"1","博客列表",sql);
	}
	
	private void otherBlogList(){
		String sql = "";
		if(DataUtils.checkString(type)){
			sql = "SELECT blog.ID,blog.IMGLIST,blog.CLASSID,blog.BLOGTITLE, DATE_FORMAT(blog.ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',"+
					"   blog.COMMENTNUMBER,blog.BROWSENUMBER,blog.BLOGSUMMY "+
					"	FROM wm_blog_info blog"+
					"	LEFT JOIN wm_blog_articleClass blogart ON blog.CLASSID=blogart.ID "+
					"	where UID = "+ouid+" AND  blog.ISPUBLIC = 1 AND blogart.CLASSLEVEL=0"+
					"   AND blog.ISDELETE = 0  ORDER BY blog.ID DESC";
		
		}else{
				//可能一次获取多个分类
				String types[] = type.split(",");
				String wheretype = "";
				for (String t:types){
					wheretype += "OR blog.CLASSID = " +t +" ";
				}
				wheretype = wheretype.substring(2);
				sql = "SELECT blog.ID,blog.IMGLIST,blog.CLASSID,blog.BLOGTITLE, DATE_FORMAT(blog.ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME',blog.COMMENTNUMBER,blog.BROWSENUMBER,blog.BLOGSUMMY "+
						"	FROM wm_blog_info blog"+
						"	LEFT JOIN wm_blog_articleClass blogart ON blog.CLASSID=blogart.ID AND blogart.CLASSLEVEL=0"+
						"	where UID = "+ouid+" and ("+wheretype+" ) AND blog.ISPUBLIC = 1  AND blog.ISDELETE = 0  ORDER BY blog.ID DESC";
				
		}
		responseInfo(page,number,"1","博客列表",sql);
	}
	
	/**
	 * 最新的博客列表
	 */
	public void newBlogList(){
		String userInfo = "SELECT NICKNAME,PHOTO,USERNAME FROM wm_user_info WHERE UID = ?";
		String sql = "SELECT UID,BLOGTITLE,ID,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') AS 'ADDTIME' FROM wm_blog_info ";
		if(!DataUtils.checkString(uid)){
			//拿uid最新的博客评论
			sql += " WHERE UID = "+uid;
		}
		sql += " ORDER BY ID DESC ";
		List<Map<String,Object>> list_value = findPageInfo(page, number, sql);
		Map<String,Object> map_value = null;
		for(Map<String,Object> map: list_value){
			map_value = middlewareService.findFirst(userInfo,map.get("uid"));
			map.putAll(map_value);
		}
		Map<String,Object> mapInfo = findPageSize(page, number, sql);
		responseInfo("1","成功",list_value,mapInfo);
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getBlogid() {
		return blogid;
	}


	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}


	public String getCommentid() {
		return commentid;
	}


	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}

	

	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}

	
}
