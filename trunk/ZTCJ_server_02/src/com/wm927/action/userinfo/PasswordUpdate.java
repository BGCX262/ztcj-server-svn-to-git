package com.wm927.action.userinfo;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import com.wm927.dbutils.ContextHolder;
import com.wm927.service.impl.MiddlewareActionService;

/**
 * 密码修改
 * @author chen
 * 修改于2013-10-22
 */
public class PasswordUpdate extends MiddlewareActionService{
	private Logger logger = Logger.getLogger(PasswordUpdate.class);
	//旧密码
	private String opassword;
	//新密码
	private String npassword;
	//用户id
	private String uid;
	
	/**
	 * 密码修改
	 * 第一步：判断字段是否为空
	 * 第二步：判断用户是否存在
	 * 第三步：修改用户密码
	 */
	public void execute(){
		Map<String,Object> checkMap = new HashMap<String,Object>();
		checkMap.put("用户ID不能为空", uid);checkMap.put("旧密码不能为空", opassword);checkMap.put("新密码不能为空", npassword);
		if(!checkNull(checkMap)){
			return;
		}
		if(!checkUser(uid)){
			return;
		}
		String checkpwd = "SELECT COUNT(*) FROM wm_user_index WHERE ID = ? AND PASSWORD = ?";
		Long  pwdcount = middlewareService.findCount(checkpwd,uid,opassword);
		if(pwdcount==0){
			responseInfo("3","密码错误！");
			return ;
		}
		String updatepwd = "UPDATE wm_user_index SET PASSWORD = ? WHERE ID = ?";
		int count = middlewareService.update(updatepwd,npassword,uid);
		if(count==0){
			logger.info("update wm_user_index false on this class --> "+this.getClass().getSimpleName());
			responseInfo("3","修改密码失败");
			return;
		}
		responseInfo("1","修改密码成功");
	}
	
	public String getOpassword() {
		return opassword;
	}
	public void setOpassword(String opassword) {
		this.opassword = opassword;
	}
	public String getNpassword() {
		return npassword;
	}
	public void setNpassword(String npassword) {
		this.npassword = npassword;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
