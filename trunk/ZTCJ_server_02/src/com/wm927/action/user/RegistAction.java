package com.wm927.action.user;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import com.wm927.commons.DateUtils;
import com.wm927.service.impl.MiddlewareActionService;

//http://localhost:8080/Middleware_server/30712.action?username=gerw&password=123&fuid
//传人关注分析师列表id，注册成功，往关注表加入分析师信息
public class RegistAction  extends MiddlewareActionService {
	Logger log = Logger.getLogger(RegistAction.class.getName());
	private String username;
	private String password;
	/**
	 */
	public void execute(){
		String sql  = "SELECT COUNT(*) FROM wm_user_index WHERE USERNAME = ?";
		String username = getParameter("username");
		String password = getParameter("password");
		Long ucount = middlewareService.findCount(sql,username);
		if(ucount != 0){
			responseInfo("-3","用户名已经存在");
			return;
		}
		if(!checkNull(new Object[]{"用户名不能为空","用户密码不能为空"},new Object[]{username,password}))
			return;
		//在mysql插入注册用户的其它信息
		String insertIndexInfoSql = " INSERT INTO wm_user_index (USERNAME,PASSWORD,REGISTIP,LASTLOGINIP,LASTLOGINTIME,LASTUPDATETIME,LASTUPDATEIP)VALUES(?,?,?,?,?,?,?)";
		int indexCount = middlewareService.update(insertIndexInfoSql,username,password,getIpAddr(),getIpAddr(),DateUtils.getCurrentTime(),DateUtils.getCurrentTime(),getIpAddr());
		if(indexCount == 0){
			responseInfo("3","添加会员信息失败");
			return;
		}
		String getUseridSql = " SELECT ID FROM wm_user_index WHERE USERNAME = ?";
		String uid = middlewareService.findBy(getUseridSql, "ID",username);
		//往wm_user_info插入当前人的uid，用户名
		String insertUserInfo = "INSERT INTO wm_user_info (UID,USERNAME) VALUES (?,?) ";
		middlewareService.update(insertUserInfo,uid,username);
		//往wm_user_communityInfo插入一条默认的数据
		String insertCommunityInfo ="INSERT INTO wm_user_communityInfo (UID)VALUES(?)";
		middlewareService. update(insertCommunityInfo, uid);
		
		List<Map<String,Object>> list =new ArrayList<Map<String,Object>>();
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("status", "Y");list.add(map);

		responseInfo("1","注册成功",list);
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
