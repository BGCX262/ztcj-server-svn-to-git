package com.wm927.action.blog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wm927.commons.DataUtils;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 博客分类
 * @author chen
 * 修改于2013-12-10
 */
public class BlogType extends MiddlewareActionService{
	//用户ID
	private String uid;
	//被查看人数据的id
	private String ouid;
	/**
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：显示博客的分类列表并且将我发表的博客在列表显示对应的数量(草稿箱为单独的一个显示)
	 * 第四步：查询我的草稿箱的数量
	 */
	public void execute(){
		if(!checkNull(new Object[]{"用户ID不能为空","他人ID不能为空"},new Object[]{uid,ouid}))
			return ; 
		
		//查询用户角色
		String roleTag = checkRole(ouid);
		if(DataUtils.checkString(roleTag)){
			responseInfo("-3","用户不存在");
				return;
		}
		
		List<Map<String,Object>> list_value = null;
		
		if(uid.equals(ouid)){
			list_value = checkBlogRole(roleTag,0);
			//查询草稿箱数量
			String tempCountSql = "SELECT COUNT(*) FROM wm_blog_info where UID = ? AND  ISPUBLIC = 0 AND ISDELETE = 0";
			Long count = middlewareService.findCount(tempCountSql,ouid);
			for(Map<String,Object> map :list_value){
				if("5".equals((String)map.get("blogclassid"))){
					map.put("blogcount", String.valueOf(count));
				}
			}
			
		}else{
			list_value =  checkBlogRole(roleTag,1);
		}
		
		responseInfo("1","成功",list_value);
	}
	
	/**
	 * 显示博客分类列表
	 * 根据角色的不同返回不同的分类
	 */
	public void showBlogType(){
		if(!checkNull(new Object[]{"用户ID不能为空"},new Object[]{uid}))
			return ; 
		//查询用户角色
		String roleTag = checkRole(uid);
		if(DataUtils.checkString(roleTag)){
			responseInfo("-3","用户不存在");
			return;
		}
		
		String sql = checkUserRole(roleTag);
		List<Map<String,Object>> list_value = middlewareService.find(sql);
		responseInfo("1","返回成功",list_value);
	}
	/**
	 * 
	 * @param role
	 * @return
	 */
	private String checkUserRole(String role){
		String findClassidSql = "SELECT CLASSID FROM wm_blog_classRole WHERE ROLETAG = ?";
		List<Map<String,Object>> list_value = middlewareService.find(findClassidSql,role);
		String where = "";
		for(Map<String,Object> map:list_value){
			where += ", "+map.get("classid");
		}
		String sql = "SELECT ID AS BLOGCLASSID,BLOGCLASSNAME FROM wm_blog_articleClass WHERE ID IN( " +where.substring(1) +" ) AND ID!=5 ORDER BY CLASSSORT";
		return sql; 
	}
	
	/**
	 * 
	 * @param role
	 * @param level 0代表本人查看，别人观看则不能获取对自己观看的权限，和草稿箱
	 * @return
	 */
	private List<Map<String,Object>> checkBlogRole(String role,int level){
		String findClassidSql = "SELECT CLASSID FROM wm_blog_classRole WHERE ROLETAG = ?";
		List<Map<String,Object>> list_value = middlewareService.find(findClassidSql,role);
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT ID AS BLOGCLASSID ,BLOGCLASSNAME ");
		sb.append(" FROM wm_blog_articleClass WHERE ");
		sb.append(" ID IN ( ");
		String where = "";
		for(Map<String,Object> map:list_value){
			where += ", "+map.get("classid");
		}
		sb.append(where.substring(1)+")");
		if(level == 1 ){
			sb.append(" AND CLASSLEVEL = 0 ");
		}
		sb.append(" ORDER BY CLASSSORT");
		List<Map<String,Object>> list_value1 = middlewareService.find(sb.toString());
		String blogCountSql = "SELECT COUNT(*) FROM wm_blog_info where UID = ? AND CLASSID = ? AND ISPUBLIC=1 AND ISDELETE=0 ";
		Long blogCount ;
		for(Map<String,Object> list:list_value1){
			blogCount = middlewareService.findCount(blogCountSql,ouid,list.get("blogclassid"));
			list.put("blogcount", blogCount+"");
		}
		
		return list_value1;
	}
	
	/**
	 * 返回草稿箱数量
	 */
	public void getDrafts(){
		if(DataUtils.checkString(uid))
		{
			responseInfo("-1","用户ID不能为空");
			return;
		}
		if(!checkUser(uid))
			return;
		String sql = "SELECT COUNT(*) FROM wm_blog_info WHERE UID = ? AND ISPUBLIC = 0 AND ISDELETE=0";
		Long count = middlewareService.findCount(sql,uid);
		List<Object> list_value = new ArrayList<Object>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("blogcount", String.valueOf(count));
		list_value.add(map);
		responseInfo("1","成功",list_value);
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getOuid() {
		return ouid;
	}

	public void setOuid(String ouid) {
		this.ouid = ouid;
	}

	
}
