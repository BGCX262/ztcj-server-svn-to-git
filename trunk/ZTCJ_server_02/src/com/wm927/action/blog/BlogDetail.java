package com.wm927.action.blog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 博客详细
 * @author chen
 * 修改于2013-10-22
 */
public class BlogDetail extends MiddlewareActionService{
	//博客id
	private String blogid;
	//用户id
	private String uid;
	private String lastid;
	private String callback;
	private String size;
	private String type = "1";
	/**
	 * 博客详细
	 * 第一步：判断字段是否为空
	 * 第二步：判断博客是否存在
	 * 第三步：更新博客点击次数，自己的不添加，游客或者他人则数量添加
	 * 第四步：查询博客的信息
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","博客ID不能为空"},new Object[]{uid,blogid}))
			return ;
		if(!checkUser(uid)){
			return ;
		}
		if(!checkBlog(blogid, "wm_blog_info", callback))
			return ;
		//验证是否自己的博客 ///博客点击一次自增一次(自己不算)
		String update = "UPDATE wm_blog_info SET BROWSENUMBER = BROWSENUMBER+1 WHERE ID = ?";
		String sql = "SELECT COUNT(*) FROM wm_blog_info WHERE ID = ? AND UID = ?";
		Long count = middlewareService.findCount(sql,blogid,uid);
		if(count == 0){
			middlewareService.update(update,blogid);
		}
		String blogSql = "SELECT ID,UID,BLOGTITLE,IMGLIST, BLOGCONTENT,BLOGSUMMY,COMMENTNUMBER ,PRAISENUMBER FROM wm_blog_info WHERE ID =  ?";
		Map<String,Object> map = middlewareService.findFirst(blogSql,blogid);
		//String commentid = "0";
		//if(!"0".equals(map.get("commentnumber"))){
		//	//将当前博客最新的评论id返回出去，防止用户显示评论数6但是实际加载的评论数已经是7了
		//	String commentSql = "SELECT ID AS COMMENTID FROM wm_blog_comment WHERE COMMENTBLOGID = ? AND COMMENTLEVEL = 1 ORDER BY ID DESC";
		//	commentid = middlewareService.findBy(commentSql, "COMMENTID" , map.get("id"));
		//}
		//获取博客的一级评论信息
		List<Map<String,Object>> list_value1 = new ArrayList<Map<String,Object>>();
		//获取博客的二级评论信息
		List<Map<String,Object>> list_value2 = new ArrayList<Map<String,Object>>();
		//获取一级评论信息时传入的是blogid
		Object obj = getCommentLevelList("0",blogid,null,null,size);
		Object laveComment = null;
		list_value1 = (List<Map<String,Object>>)obj;
		for(Map<String,Object> map1:list_value1){
			list_value2 = (List<Map<String,Object>>)map1.get("data");
			laveComment = map1.get("lavecomment");
			for(Map<String,Object> map2:list_value2){
				//根据得到的一级评论获取对应的二级评论
				//获取二级评论信息时传入的是commentid
				obj = getCommentLevelList("0",null,map2.get("commentid"),null,size);
				//然后将二级评论放入一级评论
				map2.put("comment2", obj);
				}
		}
				
		//map.put("commentid", commentid);
		List<Map<String,Object>> list_value = new ArrayList<Map<String,Object>>();
		map.put("comment1", list_value2);
		map.put("laveComment", laveComment);
		list_value.add(map);
		responseInfo("1","成功",list_value,callback);
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

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}
	public String getLastid() {
		return lastid;
	}
	public void setLastid(String lastid) {
		this.lastid = lastid;
	}


	public String getSize() {
		return size;
	}


	public void setSize(String size) {
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
}
