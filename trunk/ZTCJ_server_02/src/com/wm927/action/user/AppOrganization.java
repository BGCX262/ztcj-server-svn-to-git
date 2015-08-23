package com.wm927.action.user;

import java.util.List;
import java.util.Map;

import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * APP机构信息
 * @author chen
 *
 */
public class AppOrganization extends MiddlewareActionService{
	private String uid;
	private String page;
	private String number;
	private String index;
	private String callback;
	private String blogid;
	
	
	public void execute(){
		if(DataUtils.checkString(uid)){
			responseInfo("-1","UID不能为空");
			return;
		}
		if(DataUtils.checkString(index)){
			responseInfo("-1","下表不能为空");
			return;
		}
		
		String check_role = "SELECT OID FROM wm_user_roleAnalyst WHERE UID = ?";
		String oid = middlewareService.findBy(check_role,"OID",uid);
		if(DataUtils.checkString(oid)){
			responseInfo("-1","该用户未绑定机构");
			return;
		}
		String sql = "SELECT KEYWORD,BLOGTITLE,BLOGCONTENT,COMMENTNUMBER,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME' ,PRAISENUMBER FROM wm_blog_info WHERE UID = ? AND COVERARTURL IS NOT NULL " +
				"ORDER BY ID DESC LIMIT " +index +" ,1" ;
		List<Map<String,Object>> list_value = middlewareService.find(sql,oid);
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 获取APP机构发表的最新的三篇博客图片(分类为APP信息),没有三篇则有几篇显示几篇
	 */
	public void findBlogHotPicture(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid},callback))
			return ; 
		String check_role = "SELECT OID FROM wm_user_roleAnalyst WHERE UID = ?";
		String oid = middlewareService.findBy(check_role,"OID",uid);
		if(!checkAppOrgRole(oid,callback))
			return ;
		String sql = "SELECT ID , IMGLIST FROM wm_blog_info WHERE UID = ? AND CLASSID = 6 AND ISDELETE=0 ORDER BY ID DESC LIMIT 3";
		List<Map<String,Object>> list_value = middlewareService.find(sql,oid);
		String imglist = "";
		for(Map<String,Object> map :list_value){
			imglist = map.get("imglist") + "";
			//若有多张图片，则只拿第一张图片
			map.put("imglist", imglist.split(",")[0]);
		}
		responseInfo("1","成功",list_value,callback);
		
	}
	
	/**
	 * 根据机构ID取热图不为空的最新3条博客
	 */
	public void findBlogHotDetail(){
		if(!checkNull(new Object[]{"博客ID不能为空"},new Object[]{blogid},callback))
			return ; 
		String sql = "SELECT KEYWORD,BLOGTITLE,BLOGCONTENT,COMMENTNUMBER,DATE_FORMAT(ADDTIME ,'%Y-%m-%d %H:%i:%s') as 'ADDTIME' ,PRAISENUMBER FROM wm_blog_info WHERE ID = ?";
		List<Map<String,Object>> list_value = middlewareService.find(sql,blogid);
		responseInfo("1","成功",list_value,callback);
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

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getBlogid() {
		return blogid;
	}

	public void setBlogid(String blogid) {
		this.blogid = blogid;
	}
	
	
}
