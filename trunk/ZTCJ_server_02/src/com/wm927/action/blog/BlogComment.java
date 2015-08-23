package com.wm927.action.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 博客评论列表
 * @author chen
 * 修改于2013-10-22
 */
public class BlogComment extends MiddlewareActionService{
	//博客id
	private String blogid;
	//第几页(默认第一页)
	private String page;
	//每页多少条数据(默认5条)
	private String number;
	//最后一条评论id
	private String size;
	private String uid;
	private String commentid;
	private String lastid;
	private String type = "0";//0代表获取博客评论，1代表获取微博评论
	private String callback;
	
	/**
	 * 第一步：判断博客评论是否为空
	 * 第二步：判断博客是否存在 
	 * 第三步：在wm_blog_comment搜索当前博客的评论
	 * 获取一级评论则传入blogid 或者二级评论则传入commentid
	 */
	public void execute(){
		responseInfo("1","成功",getCommentLevelList(type,blogid,commentid,lastid,size));
	}
	
	/**
	 * 显示一级和二级博客评论，另做动态加载评论功能
	 */
	 @SuppressWarnings({ "unused", "unchecked" })
	public void allCommontList(){
		//获取博客的一级评论信息
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		//获取博客的二级评论信息
		List<Map<String,Object>> list_value2 = new ArrayList<Map<String,Object>>();
		//获取一级评论信息时传入的是blogid
		Object obj = getCommentLevelList(type,blogid,null,lastid,size);
		Map<String,Object> infoMap = new HashMap<String,Object>();
		list_value = (List<Map<String,Object>>)obj;
		for(Map<String,Object> map:list_value){
			list_value2 = (List<Map<String,Object>>)map.get("data");
			for(Map<String,Object> map1:list_value2){
				//根据得到的一级评论获取对应的二级评论
				//获取二级评论信息时传入的是commentid
				obj = getCommentLevelList(type,null,map1.get("commentid"),null,size);
				//然后将二级评论放入一级评论
				map1.put("comment2", obj);
				}
		}
		responseInfo("1","成功",list_value,callback);
	}
	
	
	public String getBlogid() {
		return blogid;
	}
	public void setBlogid(String blogid) {
		this.blogid = blogid;
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
	public String getLastid() {
		return lastid;
	}
	public void setLastid(String lastid) {
		this.lastid = lastid;
	}
	public String getCommentid() {
		return commentid;
	}
	public void setCommentid(String commentid) {
		this.commentid = commentid;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getCallback() {
		return callback;
	}
	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	
}
